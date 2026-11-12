package com.grocery.implementation;

//package com.grocery.implementation.controller;

import com.grocery.implementation.controller.CartController;
import com.grocery.implementation.model.UserModel;
import com.grocery.implementation.repository.UserRepository;
import com.grocery.implementation.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @MockBean
    private UserRepository userRepository;

    private UserModel mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new UserModel();
        mockUser.setId(1);
        mockUser.setUsername("testuser");

        // Mock the user lookup that happens in getCurrentCustomerId
        when(userRepository.findByUsername("testuser")).thenReturn(mockUser);
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("ADD: Should redirect to products with success message")
    void addItemToCart_Success() throws Exception {
        // Arrange
        when(cartService.addProductToCart(1, 101, 2)).thenReturn("SUCCESS");

        // Act & Assert
        mockMvc.perform(post("/cart/add")
                        .param("productId", "101")
                        .param("quantity", "2")
                        .with(csrf())) // Required because of Spring Security
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"))
                .andExpect(flash().attribute("cartMessage", "Added to cart!"))
                .andExpect(flash().attribute("messageType", "success"));

        verify(cartService).addProductToCart(1, 101, 2);
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("ADD: Should handle LIMIT_EXCEEDED error")
    void addItemToCart_LimitExceeded() throws Exception {
        // Arrange
        when(cartService.addProductToCart(1, 101, 1)).thenReturn("LIMIT_EXCEEDED");

        // Act & Assert
        mockMvc.perform(post("/cart/add")
                        .param("productId", "101")
                        .param("quantity", "1")
                        .with(csrf()))
                .andExpect(flash().attribute("cartMessage", "Error: Maximum limit is 5 units per item."))
                .andExpect(flash().attribute("messageType", "error"))
                .andExpect(redirectedUrl("/products"));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("REMOVE: Should call service and redirect to cart")
    void removeItem_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/cart/remove")
                        .param("cartItemId", "50")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService).removeCartItem(50, 1);
    }


}