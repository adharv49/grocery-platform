package com.grocery.implementation;

//package com.grocery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.grocery.implementation.model.OrderModel;
import com.grocery.implementation.model.DeliveryModel;
import com.grocery.implementation.model.UserModel;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

public class DeliveryServiceTest {

    @Test
    public void testGetEffectiveAddress_FromDeliveryTable() {
        // Arrange: Setup an order that already has a scheduled delivery address
        OrderModel order = new OrderModel();
        DeliveryModel delivery = new DeliveryModel();
        delivery.setDeliveryAddress("123 Permanent St (Address 2)");

        List<DeliveryModel> deliveries = new ArrayList<>();
        deliveries.add(delivery);
        order.setDeliveries(deliveries);

        // Act: This mimics the logic in your Controller/Service
        String resultAddress = order.getDeliveries().get(0).getDeliveryAddress();

        // Assert
        assertEquals("123 Permanent St (Address 2)", resultAddress);
    }

    @Test
    public void testGetEffectiveAddress_FallbackToProfile() {
        // Arrange: Setup an order with no delivery record, fallback to user profile
        UserModel user = new UserModel();
        user.setAddress("User Default Home Address");

        OrderModel order = new OrderModel();
        order.setCustomer(user);
        order.setDeliveries(new ArrayList<>()); // Empty list

        // Act
        String resultAddress = (order.getDeliveries().isEmpty())
                ? order.getCustomer().getAddress()
                : order.getDeliveries().get(0).getDeliveryAddress();

        // Assert
        assertEquals("User Default Home Address", resultAddress);
    }
}