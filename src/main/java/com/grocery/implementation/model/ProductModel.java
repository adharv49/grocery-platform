package com.grocery.implementation.model;


import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product")
public class ProductModel {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "productId")
    private Integer productId;

    @Column(name = "productName",length = 100, nullable = false)
    private String productName;

    @Column(length = 50,name = "category")
    private String category;


    @Column(precision = 10, scale = 2,name = "price")
    private BigDecimal price;

    @Column(name = "stockQuantity")
    private Integer stockQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    public enum Status {
        AVAILABLE,
        OUT_OF_STOCK
    }

    public ProductModel() {}

    public ProductModel(String productName, String category, BigDecimal price, Integer stockQuantity, Status status) {
        this.productName = productName;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.status = status;
    }

    // Getters and Setters
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = (category != null) ? category.toUpperCase().trim() : null; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
