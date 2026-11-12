package com.grocery.implementation.repository;

import com.grocery.implementation.model.DeliveryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface DeliveryRepository extends JpaRepository<DeliveryModel, Integer> {
    // Basic CRUD operations are inherited.
    // Counts how many deliveries an agent has for a specific slot on a specific date
    @Query("SELECT COUNT(d) FROM DeliveryModel d WHERE d.timeSlot = :slot AND d.deliveryDate = :date")
    long countByTimeSlotAndDeliveryDate(@Param("slot") String slot, @Param("date") LocalDate date);

    @Query("SELECT COUNT(d) FROM DeliveryModel d WHERE d.timeSlot = :slot AND d.deliveryDate = :date")
    long countOrdersForAgentInSlot(@Param("slot") String slot, @Param("date") LocalDate date);

    @Query("SELECT COUNT(d) FROM DeliveryModel d WHERE d.timeSlot = :slot " +
            "AND d.deliveryDate = :date " +
            "AND d.deliveryStatus != com.grocery.implementation.model.DeliveryModel.DeliveryStatus.CANCELLED")
    long countActiveOrdersForAgentInSlot(@Param("slot") String slot, @Param("date") LocalDate date);
}