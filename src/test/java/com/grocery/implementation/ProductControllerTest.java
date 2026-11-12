package com.grocery.implementation;

//package com.grocery.implementation.controller;

import com.grocery.implementation.controller.ProductController;
import com.grocery.implementation.model.ProductModel;
import com.grocery.implementation.repository.DeliveryRepository;
import com.grocery.implementation.repository.OrderRepository;
import com.grocery.implementation.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private OrderRepository orderRepository; // Required by constructor

    @MockBean
    private DeliveryRepository deliveryRepository; // Required by constructor

    @Autowired
    private ObjectMapper objectMapper; // Converts Objects to JSON

    private ProductModel mockProduct;

    @BeforeEach
    void setUp() {
        mockProduct = new ProductModel();
        mockProduct.setProductId(101);
        mockProduct.setProductName("Organic Apples");
        mockProduct.setPrice(new BigDecimal("2.99"));
        mockProduct.setStatus(ProductModel.Status.AVAILABLE);
    }

    @Test
    @WithMockUser
    @DisplayName("GET: Should return list of all products")
    void getAllProducts_Success() throws Exception {
        when(productService.getAllProducts()).thenReturn(Arrays.asList(mockProduct));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].productName").value("Organic Apples"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("GET: Should return 404 when product not found")
    void getProductById_NotFound() throws Exception {
        when(productService.getProductById(999)).thenReturn(null);

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST: Should save product and return JSON")
    void saveProduct_Success() throws Exception {
        when(productService.saveProduct(any(ProductModel.class))).thenReturn(mockProduct);

        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockProduct))) // Convert Java Object to JSON
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Organic Apples"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE: Should return success string")
    void deleteProduct_Success() throws Exception {
        mockMvc.perform(delete("/api/products/101")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Product deleted successfully!"));
    }
}
