import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Main application entry point for the Qveto Java Team Project.
 * 
 * Initializes a lightweight HTTP server using built-in Java standard library components
 * to serve a clean starter webpage.
 * 
 * Instructions to run:
 *   1. Compile: javac Main.java
 *   2. Run:     java Main
 *   3. Open:    http://localhost:8080 in your browser
 */
public class Main {
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new WebPageHandler());
        server.setExecutor(null); // Use default executor

        System.out.println("==================================================");
        System.out.println("  Qveto Java Team Project - Server Started");
        System.out.println("  Access URL: http://localhost:" + PORT);
        System.out.println("==================================================");

        server.start();
    }

    /**
     * HTTP handler serving the initial starter white screen webpage.
     */
    static class WebPageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String htmlContent = "<!DOCTYPE html>\n"
                    + "<html lang=\"en\">\n"
                    + "<head>\n"
                    + "    <meta charset=\"UTF-8\">\n"
                    + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                    + "    <title>Qveto Java Team Project</title>\n"
                    + "    <style>\n"
                    + "        * {\n"
                    + "            box-sizing: border-box;\n"
                    + "            margin: 0;\n"
                    + "            padding: 0;\n"
                    + "        }\n"
                    + "        body {\n"
                    + "            background-color: #ffffff;\n"
                    + "            color: #24292f;\n"
                    + "            font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, Helvetica, Arial, sans-serif;\n"
                    + "            display: flex;\n"
                    + "            justify-content: center;\n"
                    + "            align-items: center;\n"
                    + "            min-height: 100vh;\n"
                    + "        }\n"
                    + "        .container {\n"
                    + "            text-align: center;\n"
                    + "            padding: 40px;\n"
                    + "            max-width: 600px;\n"
                    + "        }\n"
                    + "        h1 {\n"
                    + "            font-size: 2.2rem;\n"
                    + "            font-weight: 600;\n"
                    + "            color: #1a1a1a;\n"
                    + "            margin-bottom: 12px;\n"
                    + "        }\n"
                    + "        p {\n"
                    + "            font-size: 1.1rem;\n"
                    + "            color: #57606a;\n"
                    + "            line-height: 1.6;\n"
                    + "        }\n"
                    + "        .status-badge {\n"
                    + "            display: inline-block;\n"
                    + "            margin-top: 24px;\n"
                    + "            padding: 8px 18px;\n"
                    + "            border-radius: 20px;\n"
                    + "            background-color: #f6f8fa;\n"
                    + "            border: 1px solid #d0d7de;\n"
                    + "            font-size: 0.95rem;\n"
                    + "            color: #0969da;\n"
                    + "            font-weight: 500;\n"
                    + "        }\n"
                    + "    </style>\n"
                    + "</head>\n"
                    + "<body>\n"
                    + "    <div class=\"container\">\n"
                    + "        <h1>Qveto Java Team Project</h1>\n"
                    + "        <p>Welcome! The Java web service is up and running.</p>\n"
                    + "        <div class=\"status-badge\">Status: Ready</div>\n"
                    + "    </div>\n"
                    + "</body>\n"
                    + "</html>\n";

            byte[] responseBytes = htmlContent.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, responseBytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }
}
