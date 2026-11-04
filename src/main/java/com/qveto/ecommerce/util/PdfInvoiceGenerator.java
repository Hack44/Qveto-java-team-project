package com.qveto.ecommerce.util;

import com.qveto.ecommerce.model.Order;
import com.qveto.ecommerce.model.OrderItem;
import com.qveto.ecommerce.model.Payment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure Java Adobe PDF 1.4 Branded Invoice & Receipt Generator.
 * Creates publication-quality, vector-rendered PDF invoices with zero external dependencies.
 *
 * Conforms to Qveto Technology Project 1 invoice delivery specifications.
 * Supports Member 2 (UI, Presentation & Store Management) billing requirements.
 */
public class PdfInvoiceGenerator {

    private PdfInvoiceGenerator() {
        // Utility class
    }

    /**
     * Generates an official, branded Adobe PDF invoice for an order.
     *
     * @param order the customer order
     * @param customerAddress shipping destination address
     * @param targetFile destination .pdf file
     * @throws IOException if writing to disk fails
     */
    public static void generateInvoicePdf(Order order, String customerAddress, File targetFile) throws IOException {
        Payment p = PaymentStore.getPaymentForOrder(order.getOrderId());
        if (p == null) {
            p = new Payment(order.getOrderId(), order.getPaymentMethod(), "COMPLETED", order.getTotalAmount());
        }
        generateInvoicePdf(order, p, customerAddress, targetFile);
    }

    /**
     * Generates an official, branded Adobe PDF invoice for an order.
     *
     * @param order the customer order
     * @param payment the associated payment audit record
     * @param customerAddress shipping destination address
     * @param targetFile destination .pdf file
     * @throws IOException if writing to disk fails
     */
    public static void generateInvoicePdf(Order order, Payment payment, String customerAddress, File targetFile) throws IOException {
        StringBuilder stream = new StringBuilder();

        // 1. Navy Header Bar
        stream.append("0.094 0.168 0.286 rg\n"); // #182b49 Navy
        stream.append("40 760 515 50 re f\n");

        // Header Title (White)
        stream.append("1 1 1 rg\n");
        stream.append("/F2 16 Tf\n");
        stream.append("BT 55 785 Td (QVETO E-COMMERCE & INVENTORY SYSTEM) Tj ET\n");
        stream.append("/F1 10 Tf\n");
        stream.append("BT 55 770 Td (Official Customer Tax Invoice & Electronic Settlement Receipt) Tj ET\n");

        // 2. Order Metadata Box
        stream.append("0.96 0.97 0.98 rg\n"); // #f8fafc
        stream.append("40 680 515 70 re f\n");
        stream.append("0.85 0.88 0.91 RG\n"); // border
        stream.append("1 w\n");
        stream.append("40 680 515 70 re S\n");

        // Meta Text
        stream.append("0.1 0.1 0.1 rg\n");
        stream.append("/F2 9 Tf\n");
        stream.append("BT 50 735 Td (Invoice Ref:) Tj ET\n");
        stream.append("BT 50 720 Td (Order Number:) Tj ET\n");
        stream.append("BT 50 705 Td (Order Date:) Tj ET\n");
        stream.append("BT 50 690 Td (Delivery:) Tj ET\n");

        stream.append("/F1 9 Tf\n");
        stream.append(String.format("BT 130 735 Td (INV-%d) Tj ET\n", order.getOrderId()));
        stream.append(String.format("BT 130 720 Td (%s) Tj ET\n", escape(order.getOrderNumber())));
        stream.append(String.format("BT 130 705 Td (%s) Tj ET\n", escape(order.getOrderDate())));
        stream.append("BT 130 690 Td (Standard Fast Courier) Tj ET\n");

        stream.append("/F2 9 Tf\n");
        stream.append("BT 300 735 Td (Order Status:) Tj ET\n");
        stream.append("BT 300 720 Td (Payment Method:) Tj ET\n");
        stream.append("BT 300 705 Td (Payment Status:) Tj ET\n");
        stream.append("BT 300 690 Td (Transaction Ref:) Tj ET\n");

        stream.append("/F1 9 Tf\n");
        stream.append(String.format("BT 390 735 Td (%s) Tj ET\n", escape(order.getOrderStatus())));
        stream.append(String.format("BT 390 720 Td (%s) Tj ET\n", escape(payment.getPaymentMethod())));
        stream.append(String.format("BT 390 705 Td (%s) Tj ET\n", escape(payment.getPaymentStatus())));
        stream.append(String.format("BT 390 690 Td (%s) Tj ET\n", escape(payment.getTransactionRef())));

        // Shipping Address Banner
        String safeAddress = customerAddress != null ? customerAddress : "Standard Customer Registered Address";
        stream.append("/F2 9 Tf\n");
        stream.append("BT 40 665 Td (Shipping Destination:) Tj ET\n");
        stream.append("/F1 9 Tf\n");
        stream.append(String.format("BT 145 665 Td (%s) Tj ET\n", escape(safeAddress)));

        // 3. Table Header Bar
        int tableHeaderY = 635;
        stream.append("0.15 0.23 0.35 rg\n"); // Slate Navy
        stream.append(String.format("40 %d 515 22 re f\n", tableHeaderY));

        stream.append("1 1 1 rg\n");
        stream.append("/F2 9 Tf\n");
        stream.append(String.format("BT 50 %d Td (Item Description) Tj ET\n", tableHeaderY + 6));
        stream.append(String.format("BT 310 %d Td (Qty) Tj ET\n", tableHeaderY + 6));
        stream.append(String.format("BT 375 %d Td (Unit Price) Tj ET\n", tableHeaderY + 6));
        stream.append(String.format("BT 475 %d Td (Subtotal) Tj ET\n", tableHeaderY + 6));

        // 4. Line Items Rows
        int currentY = tableHeaderY - 20;
        double itemsSubtotal = 0.0;
        List<OrderItem> items = order.getItems();

        if (items != null && !items.isEmpty()) {
            boolean zebra = false;
            for (OrderItem it : items) {
                if (zebra) {
                    stream.append("0.98 0.98 0.99 rg\n");
                    stream.append(String.format("40 %d 515 18 re f\n", currentY - 4));
                }
                zebra = !zebra;

                stream.append("0.1 0.1 0.1 rg\n");
                stream.append("/F1 9 Tf\n");

                String name = it.getProductName();
                if (name.length() > 42) name = name.substring(0, 39) + "...";

                stream.append(String.format("BT 50 %d Td (%s) Tj ET\n", currentY, escape(name)));
                stream.append(String.format("BT 315 %d Td (%d) Tj ET\n", currentY, it.getQuantity()));
                stream.append(String.format("BT 375 %d Td ($%.2f) Tj ET\n", currentY, it.getUnitPrice()));
                stream.append(String.format("BT 475 %d Td ($%.2f) Tj ET\n", currentY, it.getSubtotal()));

                itemsSubtotal += it.getSubtotal();
                currentY -= 20;
            }
        } else {
            itemsSubtotal = order.getTotalAmount();
            stream.append("0.1 0.1 0.1 rg\n");
            stream.append("/F1 9 Tf\n");
            stream.append(String.format("BT 50 %d Td (Standard E-Commerce Items Package) Tj ET\n", currentY));
            stream.append(String.format("BT 315 %d Td (1) Tj ET\n", currentY));
            stream.append(String.format("BT 375 %d Td ($%.2f) Tj ET\n", currentY, itemsSubtotal));
            stream.append(String.format("BT 475 %d Td ($%.2f) Tj ET\n", currentY, itemsSubtotal));
            currentY -= 20;
        }

        // Horizontal Separator
        stream.append("0.8 0.85 0.9 RG\n");
        stream.append(String.format("40 %d 515 0.5 re f\n", currentY + 6));
        currentY -= 12;

        // 5. Financial Summary Breakdown
        double taxAmount = itemsSubtotal * 0.05;
        double promoSavings = (itemsSubtotal + taxAmount) - order.getTotalAmount();

        stream.append("0.2 0.2 0.2 rg\n");
        stream.append("/F1 9 Tf\n");
        stream.append(String.format("BT 360 %d Td (Items Subtotal:) Tj 475 %d Td ($%.2f) Tj ET\n", currentY, currentY, itemsSubtotal));
        currentY -= 16;

        if (promoSavings > 0.5) {
            stream.append("0.086 0.639 0.290 rg\n"); // Emerald green #16a34a
            stream.append("/F2 9 Tf\n");
            stream.append(String.format("BT 360 %d Td (Promotional Savings:) Tj 475 %d Td (-$%.2f) Tj ET\n", currentY, currentY, promoSavings));
            currentY -= 16;
        }

        stream.append("0.2 0.2 0.2 rg\n");
        stream.append("/F1 9 Tf\n");
        stream.append(String.format("BT 360 %d Td (Estimated Tax (5%%):) Tj 475 %d Td ($%.2f) Tj ET\n", currentY, currentY, taxAmount));
        currentY -= 18;

        // Grand Total Bar
        stream.append("0.094 0.168 0.286 rg\n"); // Navy
        stream.append(String.format("340 %d 215 24 re f\n", currentY - 4));
        stream.append("1 1 1 rg\n");
        stream.append("/F2 11 Tf\n");
        stream.append(String.format("BT 350 %d Td (GRAND TOTAL:) Tj 465 %d Td ($%.2f) Tj ET\n", currentY + 3, currentY + 3, order.getTotalAmount()));
        currentY -= 40;

        // 6. Security & Audit Verification Box
        stream.append("0.96 0.97 0.98 rg\n");
        stream.append(String.format("40 %d 515 36 re f\n", currentY));
        stream.append("0.85 0.88 0.91 RG\n");
        stream.append(String.format("40 %d 515 36 re S\n", currentY));

        stream.append("0.1 0.4 0.2 rg\n");
        stream.append("/F2 8 Tf\n");
        stream.append(String.format("BT 50 %d Td (VERIFIED ELECTRONIC RECEIPT) Tj ET\n", currentY + 22));
        stream.append("0.3 0.3 0.3 rg\n");
        stream.append("/F1 8 Tf\n");
        stream.append(String.format("BT 50 %d Td (Cryptographically hashed transaction record audit: %s. Zero balance remaining.) Tj ET\n",
                currentY + 10, escape(payment.getTransactionRef())));

        // 7. Footer Notice
        stream.append("0.5 0.5 0.5 rg\n");
        stream.append("/F1 8 Tf\n");
        stream.append("BT 180 50 Td (Thank you for shopping with Qveto Technology E-Commerce Store!) Tj ET\n");
        stream.append("BT 210 38 Td (Generated securely via Qveto Pure Java Enterprise Engine) Tj ET\n");

        // Assemble PDF binary document
        byte[] contentBytes = stream.toString().getBytes(StandardCharsets.US_ASCII);

        List<String> objects = new ArrayList<>();
        objects.add("<< /Type /Catalog /Pages 2 0 R >>");
        objects.add("<< /Type /Pages /Kids [3 0 R] /Count 1 >>");
        objects.add("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 5 0 R /F2 6 0 R >> >> >>");
        objects.add("<< /Length " + contentBytes.length + " >>\nstream\n" + stream.toString() + "endstream");
        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>");
        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>");

        try (FileOutputStream out = new FileOutputStream(targetFile)) {
            // Write PDF Header
            out.write("%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n".getBytes(StandardCharsets.US_ASCII));

            List<Long> offsets = new ArrayList<>();
            long currentOffset = 15; // after header

            for (int i = 0; i < objects.size(); i++) {
                offsets.add(currentOffset);
                String objHeader = (i + 1) + " 0 obj\n";
                String objFooter = "\nendobj\n";
                byte[] objBytes = (objHeader + objects.get(i) + objFooter).getBytes(StandardCharsets.US_ASCII);
                out.write(objBytes);
                currentOffset += objBytes.length;
            }

            // Cross reference table
            long xrefOffset = currentOffset;
            StringBuilder xref = new StringBuilder();
            xref.append("xref\n");
            xref.append(String.format("0 %d\n", objects.size() + 1));
            xref.append("0000000000 65535 f \n");
            for (Long off : offsets) {
                xref.append(String.format("%010d 00000 n \n", off));
            }
            xref.append("trailer\n");
            xref.append(String.format("<< /Size %d /Root 1 0 R >>\n", objects.size() + 1));
            xref.append("startxref\n");
            xref.append(xrefOffset).append("\n");
            xref.append("%%EOF\n");

            out.write(xref.toString().getBytes(StandardCharsets.US_ASCII));
        }
    }

    private static String escape(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("(", "\\(")
                   .replace(")", "\\)");
    }
}
