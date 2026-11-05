package com.qveto.ecommerce.util;

import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-Safe, Resilient JDBC Connection Pool with Transparent Proxy Recycling.
 *
 * Key Capabilities:
 * 1. Proxy-based recycling: When callers invoke {@code connection.close()}, the proxy
 *    intercepts the call and returns the underlying physical connection back to the idle
 *    pool instead of severing the socket.
 * 2. Pre-lease health validation: Each recycled connection is tested with {@code isValid(2)}
 *    before lease. Stale or broken connections are discarded and freshly re-established.
 * 3. Dynamic elasticity: Starts with {@code initialSize} connections and expands on-demand
 *    up to {@code maxSize} under concurrent traffic spikes.
 * 4. Graceful drain & shutdown: Safely closes all underlying physical sockets on application exit.
 * 5. Full observability: Provides real-time metrics for active leases, idle count, and lifetime creations.
 *
 * Member 1 — Persistence & Database Infrastructure layer.
 */
public class DBConnectionPool {

    private static final int DEFAULT_INITIAL_SIZE = 3;
    private static final int DEFAULT_MAX_SIZE = 10;
    private static final long DEFAULT_TIMEOUT_MS = 5000L;
    private static final int DEFAULT_VALIDATION_TIMEOUT_SEC = 2;

    private static volatile DBConnectionPool instance;
    private static final Object LOCK = new Object();

    private final String url;
    private final String user;
    private final String password;
    private final int initialSize;
    private final int maxSize;
    private final long connectionTimeoutMs;
    private final int validationTimeoutSec;

    private final BlockingQueue<Connection> idlePool;
    private final Set<Connection> activeLeases = Collections.synchronizedSet(new HashSet<>());
    private final AtomicInteger totalConnectionsCreated = new AtomicInteger(0);
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    /**
     * Singleton accessor. Loads configuration from db.properties if available.
     */
    public static DBConnectionPool getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = createFromProperties();
                }
            }
        }
        return instance;
    }

    private static DBConnectionPool createFromProperties() {
        Properties props = new Properties();
        try (InputStream in = DBConnectionPool.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception ignored) {}

        String url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/ecommerce_db");
        String user = props.getProperty("db.user", "root");
        String password = props.getProperty("db.password", "");

        int initSize = getIntProp(props, "db.pool.initialSize", DEFAULT_INITIAL_SIZE);
        int maxSize = getIntProp(props, "db.pool.maxSize", DEFAULT_MAX_SIZE);
        long timeout = getLongProp(props, "db.pool.timeout", DEFAULT_TIMEOUT_MS);
        int valTimeout = getIntProp(props, "db.pool.validationTimeout", DEFAULT_VALIDATION_TIMEOUT_SEC);

        return new DBConnectionPool(url, user, password, initSize, maxSize, timeout, valTimeout);
    }

    private static int getIntProp(Properties p, String key, int def) {
        String val = p.getProperty(key);
        if (val != null) {
            try { return Integer.parseInt(val.trim()); } catch (NumberFormatException ignored) {}
        }
        return def;
    }

    private static long getLongProp(Properties p, String key, long def) {
        String val = p.getProperty(key);
        if (val != null) {
            try { return Long.parseLong(val.trim()); } catch (NumberFormatException ignored) {}
        }
        return def;
    }

    public DBConnectionPool(String url, String user, String password,
                            int initialSize, int maxSize, long connectionTimeoutMs, int validationTimeoutSec) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.initialSize = Math.max(1, initialSize);
        this.maxSize = Math.max(this.initialSize, maxSize);
        this.connectionTimeoutMs = Math.max(500L, connectionTimeoutMs);
        this.validationTimeoutSec = Math.max(1, validationTimeoutSec);

        this.idlePool = new ArrayBlockingQueue<>(this.maxSize);

        // Pre-warm the pool if credentials are valid
        warmPool();
    }

    private void warmPool() {
        if (password == null || password.isBlank() || password.equals("REPLACE_WITH_YOUR_MYSQL_PASSWORD")) {
            return;
        }

        try {
            for (int i = 0; i < initialSize; i++) {
                Connection c = createPhysicalConnection();
                if (c != null) {
                    idlePool.offer(c);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DBConnectionPool] Pool pre-warming deferred: " + e.getMessage());
        }
    }

    private Connection createPhysicalConnection() throws SQLException {
        if (totalConnectionsCreated.get() >= maxSize) {
            return null;
        }
        Connection conn = DriverManager.getConnection(url, user, password);
        totalConnectionsCreated.incrementAndGet();
        return conn;
    }

    /**
     * Leases a connection from the pool wrapped in a recycling proxy.
     *
     * @return a proxy {@link Connection} that returns to the pool on {@code close()}
     * @throws SQLException if a connection cannot be acquired within the timeout
     */
    public Connection getConnection() throws SQLException {
        if (isShutdown.get()) {
            throw new SQLException("Cannot acquire connection: DBConnectionPool has been shut down.");
        }

        long deadline = System.currentTimeMillis() + connectionTimeoutMs;

        while (System.currentTimeMillis() < deadline) {
            Connection physical = idlePool.poll();

            if (physical != null) {
                // Validate existing connection
                if (isConnectionHealthy(physical)) {
                    activeLeases.add(physical);
                    return wrap(physical);
                } else {
                    // Stale or dead: close it physically and decrement counter
                    closeQuietly(physical);
                    totalConnectionsCreated.decrementAndGet();
                }
            }

            // Pool is empty — try to create a new one if below maxSize
            synchronized (this) {
                if (totalConnectionsCreated.get() < maxSize) {
                    try {
                        Connection newlyCreated = createPhysicalConnection();
                        if (newlyCreated != null) {
                            activeLeases.add(newlyCreated);
                            return wrap(newlyCreated);
                        }
                    } catch (SQLException e) {
                        // Let outer loop wait or throw if deadline reached
                    }
                }
            }

            // Wait briefly for an idle connection to become available
            long remaining = deadline - System.currentTimeMillis();
            if (remaining > 0) {
                try {
                    Connection waited = idlePool.poll(Math.min(remaining, 200L), TimeUnit.MILLISECONDS);
                    if (waited != null) {
                        if (isConnectionHealthy(waited)) {
                            activeLeases.add(waited);
                            return wrap(waited);
                        } else {
                            closeQuietly(waited);
                            totalConnectionsCreated.decrementAndGet();
                        }
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Interrupted while waiting for database connection from pool", ie);
                }
            }
        }

        throw new SQLException(String.format(
                "Connection pool exhausted (Timeout %d ms). Active leases: %d, Idle: %d, Max: %d",
                connectionTimeoutMs, activeLeases.size(), idlePool.size(), maxSize
        ));
    }

    private boolean isConnectionHealthy(Connection c) {
        try {
            if (c.isClosed()) return false;
            return c.isValid(validationTimeoutSec);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Reclaims a physical connection from a closed proxy and restores it to the idle queue.
     */
    protected void releaseConnection(Connection physical) {
        if (physical == null) return;

        activeLeases.remove(physical);

        if (isShutdown.get()) {
            closeQuietly(physical);
            totalConnectionsCreated.decrementAndGet();
            return;
        }

        try {
            // Reset auto-commit to default true in case caller left it false
            if (!physical.isClosed()) {
                if (!physical.getAutoCommit()) {
                    try {
                        physical.rollback();
                    } catch (SQLException ignored) {}
                    physical.setAutoCommit(true);
                }

                boolean offered = idlePool.offer(physical);
                if (!offered) {
                    // Pool queue is unexpectedly full; dispose of this excess connection
                    closeQuietly(physical);
                    totalConnectionsCreated.decrementAndGet();
                }
            } else {
                totalConnectionsCreated.decrementAndGet();
            }
        } catch (SQLException e) {
            closeQuietly(physical);
            totalConnectionsCreated.decrementAndGet();
        }
    }

    /**
     * Creates a dynamic proxy wrapping the physical connection.
     * Intercepts close(), isClosed(), and unwrap() methods.
     */
    private Connection wrap(final Connection physical) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                new InvocationHandler() {
                    private boolean closed = false;

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String methodName = method.getName();

                        if ("close".equals(methodName)) {
                            if (!closed) {
                                closed = true;
                                releaseConnection(physical);
                            }
                            return null;
                        }

                        if ("isClosed".equals(methodName)) {
                            return closed || physical.isClosed();
                        }

                        if (closed) {
                            throw new SQLException("Connection has been returned to pool and is closed.");
                        }

                        if ("unwrap".equals(methodName) && args != null && args.length == 1) {
                            Class<?> iface = (Class<?>) args[0];
                            if (iface.isInstance(physical)) {
                                return physical;
                            }
                        }

                        if ("isWrapperFor".equals(methodName) && args != null && args.length == 1) {
                            Class<?> iface = (Class<?>) args[0];
                            return iface.isInstance(physical);
                        }

                        try {
                            return method.invoke(physical, args);
                        } catch (java.lang.reflect.InvocationTargetException ite) {
                            throw ite.getCause();
                        }
                    }
                }
        );
    }

    /**
     * Safely drains and shuts down the connection pool.
     */
    public void shutdown() {
        if (isShutdown.compareAndSet(false, true)) {
            Connection c;
            while ((c = idlePool.poll()) != null) {
                closeQuietly(c);
            }

            synchronized (activeLeases) {
                for (Connection active : activeLeases) {
                    closeQuietly(active);
                }
                activeLeases.clear();
            }
            totalConnectionsCreated.set(0);
        }
    }

    private static void closeQuietly(Connection c) {
        if (c != null) {
            try {
                if (!c.isClosed()) {
                    c.close();
                }
            } catch (SQLException ignored) {}
        }
    }

    // ---------------------------------------------------------------
    // Diagnostics & Observability
    // ---------------------------------------------------------------

    public int getActiveCount() {
        return activeLeases.size();
    }

    public int getIdleCount() {
        return idlePool.size();
    }

    public int getTotalCreated() {
        return totalConnectionsCreated.get();
    }

    public int getMaxSize() {
        return maxSize;
    }

    public boolean isShutdown() {
        return isShutdown.get();
    }

    public String getPoolStats() {
        return String.format("Pool[Active: %d, Idle: %d, Total: %d, Max: %d]",
                getActiveCount(), getIdleCount(), getTotalCreated(), getMaxSize());
    }

    /**
     * Resets the singleton instance (useful for clean testing lifecycle).
     */
    public static void resetInstance() {
        synchronized (LOCK) {
            if (instance != null) {
                instance.shutdown();
                instance = null;
            }
        }
    }
}
