package com.grocery.implementation.repository;

import com.grocery.implementation.model.OrderModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderModel, Integer> {

    // Spring Data JPA automatically provides methods like:
    // List<OrderItemModel> findByOrder_OrderId(Integer orderId);

    // You can use the above method in your OrderService if you wanted to load items separately.
}