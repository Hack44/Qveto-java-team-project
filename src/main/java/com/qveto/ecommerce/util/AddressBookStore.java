package com.qveto.ecommerce.util;

import com.qveto.ecommerce.model.Address;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Centralized Store for managing customer saved shipping and billing addresses.
 * Provides default address designation, multi-destination lookups, and checkout pre-population.
 *
 * Supports Member 2 (UI, Presentation & Store Management) address book requirements.
 */
public class AddressBookStore {

    private static final Map<Integer, List<Address>> userAddresses = new ConcurrentHashMap<>();
    private static final AtomicInteger nextId = new AtomicInteger(10);

    static {
        seedSampleAddresses();
    }

    private AddressBookStore() {
        // Utility class
    }

    private static void seedSampleAddresses() {
        userAddresses.clear();

        List<Address> customer1 = new CopyOnWriteArrayList<>();
        customer1.add(new Address(1, 1, "Home", "Alex Morgan",
                "124 Tech Park Road, Whitefield", "Bengaluru", "Karnataka", "560066",
                "+91 9876543210", true));

        customer1.add(new Address(2, 1, "Work", "Alex Morgan",
                "Tower B, Innovation Cyber City, Phase 1", "Bengaluru", "Karnataka", "560100",
                "+91 9876543211", false));

        userAddresses.put(1, customer1);
    }

    public static List<Address> getAddresses(int userId) {
        return userAddresses.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>());
    }

    public static Address getDefaultAddress(int userId) {
        List<Address> list = getAddresses(userId);
        for (Address a : list) {
            if (a.isDefault()) return a;
        }
        return list.isEmpty() ? null : list.get(0);
    }

    public static Address addAddress(Address address) {
        if (address == null) return null;
        address.setAddressId(nextId.incrementAndGet());

        List<Address> list = getAddresses(address.getUserId());
        if (address.isDefault() || list.isEmpty()) {
            for (Address a : list) {
                a.setDefault(false);
            }
            address.setDefault(true);
        }
        list.add(address);
        return address;
    }

    public static boolean updateAddress(Address address) {
        if (address == null) return false;
        List<Address> list = getAddresses(address.getUserId());

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getAddressId() == address.getAddressId()) {
                if (address.isDefault()) {
                    for (Address a : list) a.setDefault(false);
                }
                list.set(i, address);
                return true;
            }
        }
        return false;
    }

    public static boolean deleteAddress(int userId, int addressId) {
        List<Address> list = getAddresses(userId);
        boolean removed = list.removeIf(a -> a.getAddressId() == addressId);

        // Ensure at least one remaining address is default if any left
        if (removed && !list.isEmpty()) {
            boolean hasDefault = false;
            for (Address a : list) {
                if (a.isDefault()) {
                    hasDefault = true;
                    break;
                }
            }
            if (!hasDefault) {
                list.get(0).setDefault(true);
            }
        }
        return removed;
    }

    public static boolean setDefault(int userId, int addressId) {
        List<Address> list = getAddresses(userId);
        boolean found = false;

        for (Address a : list) {
            if (a.getAddressId() == addressId) {
                a.setDefault(true);
                found = true;
            } else {
                a.setDefault(false);
            }
        }
        return found;
    }

    public static int count(int userId) {
        return getAddresses(userId).size();
    }
}
