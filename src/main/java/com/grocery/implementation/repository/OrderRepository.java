package com.grocery.implementation.repository;

import com.grocery.implementation.model.OrderModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderModel, Integer> {

    // You can add custom queries here if needed, but JpaRepository provides basic findById(Integer), save(OrderModel), etc.

    // Example of a custom method to eagerly fetch related data for details page:
    // @Query("SELECT o FROM OrderModel o JOIN FETCH o.orderItems oi WHERE o.orderId = :orderId")
    // Optional<OrderModel> findByIdWithDetails(Integer orderId);
    List<OrderModel> findByCustomer_IdOrderByOrderDateDesc(Integer customerId);
}