package com.grocery.implementation.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "OrderInfo")
public class OrderModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerId", nullable = false)
    private UserModel customer;

    private LocalDate orderDate;
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
   // @Column(columnDefinition = "ENUM('PLACED','CANCELLED','DELIVERED')")
    @Column(name = "status", length = 50)
    private OrderStatus status;

    // --- Relationships ---

    // 1. One Order has Many Items
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemModel> orderItems = new ArrayList<>();

    // 2. One Order has One Delivery
   // @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    //private DeliveryModel delivery;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DeliveryModel> deliveries; // Changed from single object to List

    // 3. One Order has One Payment
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private PaymentModel payment;


    // Inside OrderModel.java
    public enum OrderStatus {
        PLACED,
        SCHEDULED,
        OUT_FOR_DELIVERY,
        DELIVERED,
        CANCELLED,
        RETURNED,
        CANCELLATION_REQUESTED
    }
    // --- Constructors ---

    public OrderModel() {} // Required by JPA

    // --- Helper Methods (Crucial for Foreign Keys) ---

    public void addOrderItem(OrderItemModel item) {
        this.orderItems.add(item);
        item.setOrder(this);
    }



    public void setDeliveries(List<DeliveryModel> deliveries) {
        this.deliveries = deliveries;
    }

    public void setPayment(PaymentModel payment) {
        this.payment = payment;
        if (payment != null) {
            payment.setOrder(this);
        }
    }

    // --- Getters and Setters ---

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }

    public UserModel getCustomer() { return customer; }
    public void setCustomer(UserModel customer) { this.customer = customer; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public List<OrderItemModel> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemModel> orderItems) { this.orderItems = orderItems; }

//    public DeliveryModel getDelivery() { return delivery; }

    public List<DeliveryModel> getDeliveries() {
        return deliveries;
    }

    public PaymentModel getPayment() { return payment; }

}