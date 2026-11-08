package com.qveto.ecommerce.model;

/**
 * Model entity representing a customer shipping/delivery address.
 * Supports Member 2 (UI, Presentation & Store Management) multi-address book requirements.
 */
public class Address {

    private int addressId;
    private int userId;
    private String label; // "Home", "Work", "Office", "Other"
    private String recipientName;
    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;
    private String phoneNumber;
    private boolean isDefault;

    public Address() {
        this.label = "Home";
        this.isDefault = false;
    }

    public Address(int addressId, int userId, String label, String recipientName,
                   String streetAddress, String city, String state, String postalCode,
                   String phoneNumber, boolean isDefault) {
        this.addressId = addressId;
        this.userId = userId;
        this.label = label != null ? label : "Home";
        this.recipientName = recipientName;
        this.streetAddress = streetAddress;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.phoneNumber = phoneNumber;
        this.isDefault = isDefault;
    }

    public int getAddressId() { return addressId; }
    public void setAddressId(int addressId) { this.addressId = addressId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getStreetAddress() { return streetAddress; }
    public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    public String getFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        if (streetAddress != null) sb.append(streetAddress);
        if (city != null && !city.isEmpty()) sb.append(", ").append(city);
        if (state != null && !state.isEmpty()) sb.append(", ").append(state);
        if (postalCode != null && !postalCode.isEmpty()) sb.append(" - ").append(postalCode);
        return sb.toString();
    }

    public String getSingleLineSummary() {
        return "[" + label + (isDefault ? " ★ Default" : "") + "] " + recipientName + " — " + getFormattedAddress();
    }

    @Override
    public String toString() {
        return getSingleLineSummary();
    }
}
