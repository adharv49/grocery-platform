package com.grocery.implementation;

//package com.grocery.implementation.controller;

import com.grocery.implementation.controller.OrderController;
import com.grocery.implementation.model.*;
import com.grocery.implementation.repository.OrderRepository;
import com.grocery.implementation.repository.UserRepository;
import com.grocery.implementation.service.CartService;
import com.grocery.implementation.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired private MockMvc mockMvc;

    // We must mock all dependencies injected via the constructor
    @MockBean private CartService cartService;
    @MockBean private OrderService orderService;
    @MockBean private OrderRepository orderRepository;
    @MockBean private UserRepository userRepository;
    @MockBean private com.grocery.implementation.service.ProductService productService;
    @MockBean private com.grocery.implementation.repository.DeliveryRepository deliveryRepository;
    @MockBean private com.grocery.implementation.service.InvoiceService invoiceService;

    private UserModel mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new UserModel();
        mockUser.setId(1);
        mockUser.setUsername("testuser");
        mockUser.setAddress("123 Street");
        mockUser.setWalletBalance(new BigDecimal("1000.00"));

        when(userRepository.findByUsername("testuser")).thenReturn(mockUser);
        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("CHECKOUT: Display form with correct total")
    void viewCheckoutForm_Success() throws Exception {
        // Arrange
        CartItemModel item = new CartItemModel();
        ProductModel product = new ProductModel();
        product.setPrice(new BigDecimal("50.00"));
        item.setProduct(product);
        item.setQuantity(2); // Total should be 100.00

        when(cartService.getCartItemsByCustomerId(1)).thenReturn(java.util.List.of(item));

        // Act & Assert
        mockMvc.perform(get("/orders/checkout"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("totalAmount", new BigDecimal("100.00")))
                .andExpect(view().name("checkout"));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("PLACE ORDER: Success redirects to confirmation")
    void createOrder_Success() throws Exception {
        // Arrange
        OrderModel mockOrder = new OrderModel();
        mockOrder.setOrderId(500);
        when(orderService.createOrder(anyInt(), any(), anyString(), anyBoolean()))
                .thenReturn(mockOrder);

        // Act & Assert
        mockMvc.perform(post("/orders/place")
                        .param("addressLine1", "New Home Address")
                        .param("paymentMethod", "UPI")
                        .param("useWallet", "true")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/500"))
                .andExpect(flash().attributeExists("orderMessage"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("ADMIN: Update order status")
    void updateOrderStatus_AdminOnly() throws Exception {
        OrderModel order = new OrderModel();
        when(orderRepository.findById(10)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/admin/orders/status")
                        .param("id", "10")
                        .param("s", "DELIVERED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/orders"));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("CANCEL: Request cancellation and redirect")
    void requestCancel_Success() throws Exception {
        mockMvc.perform(post("/orders/request-cancel")
                        .param("orderId", "10")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/history"));
    }
}
