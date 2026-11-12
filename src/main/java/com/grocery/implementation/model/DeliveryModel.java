package com.grocery.implementation.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Delivery") // Matches your SQL table name
public class DeliveryModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer deliveryId;

   // @OneToOne(fetch = FetchType.LAZY)
    @ManyToOne
    @JoinColumn(name = "orderId", nullable = false)
    private OrderModel order;

    private LocalDate deliveryDate;
    private String deliveryAddress; // 🎯 Add this column


    private String timeSlot;

    @Enumerated(EnumType.STRING)
    //@Column(columnDefinition = "ENUM('SCHEDULED','OUT_FOR_DELIVERY','DELIVERED','CANCELLED')",length = 200)
    private DeliveryStatus deliveryStatus; // Maps to SQL ENUM

    public enum DeliveryStatus {
        SCHEDULED, OUT_FOR_DELIVERY, DELIVERED,CANCELLED
    }

    // --- Getters and Setters (Required) ---
    // (Omitted for brevity)

    public Integer getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(Integer deliveryId) {
        this.deliveryId = deliveryId;
    }

    public OrderModel getOrder() {
        return order;
    }

    public void setOrder(OrderModel order) {
        this.order = order;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }
}