package com.grocery.implementation.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

// Assuming a table named 'OrderItem' or similar for the line items
@Entity
@Table(name = "OrderItem")
public class OrderItemModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderItemId;

    // Many-to-one relationship back to the parent order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", nullable = false)
    private OrderModel order;

    // Snapshot data to keep historical integrity
    private Integer productId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;

    // --- Constructors ---
    public OrderItemModel() {}

    // --- Getters and Setters ---
    public Integer getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Integer orderItemId) { this.orderItemId = orderItemId; }
    public OrderModel getOrder() { return order; }
    public void setOrder(OrderModel order) { this.order = order; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}