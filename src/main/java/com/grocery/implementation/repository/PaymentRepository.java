package com.grocery.implementation.repository;

import com.grocery.implementation.model.PaymentModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentModel, Integer> {
    // Basic CRUD operations are inherited.
}