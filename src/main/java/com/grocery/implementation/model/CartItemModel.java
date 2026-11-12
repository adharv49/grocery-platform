package com.grocery.implementation.model;

import jakarta.persistence.*;

@Entity
@Table(name = "CartItem")
public class CartItemModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cartItemId;

    // Assuming you have a UserModel for the customer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerId", nullable = false)
    private UserModel customer; // Maps to customerId column

    // Assuming you have a ProductModel
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productId", nullable = false)
    private ProductModel product; // Maps to productId column

    private Integer quantity; // Maps to quantity column

    // --- Constructors, Getters, and Setters ---

    // Default constructor required by JPA
    public CartItemModel() {}

    // Constructor for creating new items
    public CartItemModel(UserModel customer, ProductModel product, Integer quantity) {
        this.customer = customer;
        this.product = product;
        this.quantity = quantity;
    }

    // Getters and Setters (omitted for brevity, but required by JPA)
    public Integer getCartItemId() { return cartItemId; }
    public void setCartItemId(Integer cartItemId) { this.cartItemId = cartItemId; }
    public UserModel getCustomer() { return customer; }
    public void setCustomer(UserModel customer) { this.customer = customer; }
    public ProductModel getProduct() { return product; }
    public void setProduct(ProductModel product) { this.product = product; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
