package com.grocery.implementation.service;

import com.grocery.implementation.model.OrderModel;
import com.grocery.implementation.model.OrderItemModel;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletResponse; // 🔑 Jakarta Import
import java.io.IOException;
import java.math.BigDecimal;

@Service
public class InvoiceService {

    public void generateInvoice(OrderModel order, String address, HttpServletResponse response) throws IOException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
        Paragraph title = new Paragraph("INVOICE", titleFont);
        title.setAlignment(Element.ALIGN_RIGHT);
        document.add(title);

        // Company & Order Info
        document.add(new Paragraph("GroceryMart Inc."));
        document.add(new Paragraph("Order ID: #" + order.getOrderId()));
        document.add(new Paragraph("Date: " + order.getOrderDate()));
        document.add(new Paragraph("Delivery Address: " + (address != null ? address : "N/A")));
        document.add(new Paragraph(" "));

        // Table for items
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        // Table Headers
        String[] headers = {"Product", "Quantity", "Price", "Subtotal"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        // Table Rows
        for (OrderItemModel item : order.getOrderItems()) {
            table.addCell(item.getProductName());
            table.addCell(String.valueOf(item.getQuantity()));
            table.addCell("Rs. " + item.getUnitPrice());

            BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            table.addCell("Rs. " + subtotal);
        }

        document.add(table);

        // Grand Total
        Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph total = new Paragraph("Grand Total: Rs. " + order.getTotalAmount(), totalFont);
        total.setAlignment(Element.ALIGN_RIGHT);
        document.add(total);

        document.close();
    }
}