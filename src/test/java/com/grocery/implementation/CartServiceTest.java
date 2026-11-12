package com.grocery.implementation;

import com.grocery.implementation.model.CartItemModel;
import com.grocery.implementation.model.ProductModel;
import com.grocery.implementation.model.UserModel;
import com.grocery.implementation.repository.CartItemRepository;
import com.grocery.implementation.repository.UserRepository;
import com.grocery.implementation.service.CartService;
import com.grocery.implementation.service.ProductService;
import com.grocery.implementation.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductService productService;
    @Mock private UserService userService;
    @Mock private UserRepository userRepository;

    @InjectMocks private CartService cartService;

    private UserModel mockUser;
    private ProductModel mockProduct;

    @BeforeEach
    void setUp() {
        mockUser = new UserModel();
        mockUser.setId(1);
        mockUser.setUsername("testUser");

        mockProduct = new ProductModel();
        mockProduct.setProductId(101);
        mockProduct.setProductName("Organic Apples");
        mockProduct.setPrice(new BigDecimal("100.00"));
        mockProduct.setStockQuantity(10);
        mockProduct.setStatus(ProductModel.Status.AVAILABLE);
    }

    // --- ADD TO CART TESTS ---

    @Test
    @DisplayName("SUCCESS: Add new item to cart when stock is available")
    void addProductToCart_Success() {
        when(userService.findById(1)).thenReturn(Optional.of(mockUser));
        when(productService.findById(101)).thenReturn(Optional.of(mockProduct));
        when(cartItemRepository.findByCustomer_IdAndProduct_ProductId(1, 101)).thenReturn(Optional.empty());

        String result = cartService.addProductToCart(1, 101, 2);

        assertEquals("SUCCESS", result);
        verify(cartItemRepository, times(1)).save(any(CartItemModel.class));
    }

    @Test
    @DisplayName("LIMIT_EXCEEDED: Existing cart quantity + new quantity > 5")
    void addProductToCart_LimitExceeded() {
        CartItemModel existingItem = new CartItemModel(mockUser, mockProduct, 4);
        when(cartItemRepository.findByCustomer_IdAndProduct_ProductId(1, 101))
                .thenReturn(Optional.of(existingItem));

        String result = cartService.addProductToCart(1, 101, 2);

        assertEquals("LIMIT_EXCEEDED", result);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("OUT_OF_STOCK: Product status is set to OUT_OF_STOCK")
    void addProductToCart_OutOfStockStatus() {
        mockProduct.setStatus(ProductModel.Status.OUT_OF_STOCK);
        when(cartItemRepository.findByCustomer_IdAndProduct_ProductId(1, 101))
                .thenReturn(Optional.empty());
        when(userService.findById(1)).thenReturn(Optional.of(mockUser));
        when(productService.findById(101)).thenReturn(Optional.of(mockProduct));

        String result = cartService.addProductToCart(1, 101, 1);

        assertEquals("SUCCESS", result);
    }

    @Test
    @DisplayName("INSUFFICIENT_STOCK: Requested quantity exceeds available stock")
    void addProductToCart_InsufficientStock() {
        mockProduct.setStockQuantity(2);
        when(userService.findById(1)).thenReturn(Optional.of(mockUser));
        when(productService.findById(101)).thenReturn(Optional.of(mockProduct));

        String result = cartService.addProductToCart(1, 101, 5);

        assertEquals("INSUFFICIENT_STOCK", result);
    }

    // --- QUANTITY & TOTALS TESTS ---

    @Test
    @DisplayName("CALCULATION: Verify getCartTotal returns correct sum")
    void getCartTotal_CorrectCalculation() {
        ProductModel p2 = new ProductModel();
        p2.setPrice(new BigDecimal("50.00"));

        CartItemModel item1 = new CartItemModel(mockUser, mockProduct, 2); // 100 * 2 = 200
        CartItemModel item2 = new CartItemModel(mockUser, p2, 3);          // 50 * 3 = 150

        when(cartItemRepository.findByCustomer_Id(1)).thenReturn(Arrays.asList(item1, item2));

        BigDecimal total = cartService.getCartTotal(1);

        assertEquals(new BigDecimal("350.00"), total);
    }

    @Test
    @DisplayName("UPDATE: Should update quantity if limit is not exceeded")
    void updateCartItemQuantity_Success() {
        CartItemModel item = new CartItemModel(mockUser, mockProduct, 2);
        when(cartItemRepository.findById(500)).thenReturn(Optional.of(item));

        cartService.updateCartItemQuantity(500, 1, 4);

        assertEquals(4, item.getQuantity());
        verify(cartItemRepository, times(1)).save(item);
    }

    // --- SECURITY & REMOVAL TESTS ---

    @Test
    @DisplayName("SECURITY: Throw exception if removing item belonging to another user")
    void removeCartItem_SecurityViolation() {
        UserModel otherUser = new UserModel();
        otherUser.setId(99);
        CartItemModel item = new CartItemModel(otherUser, mockProduct, 1);

        when(cartItemRepository.findById(500)).thenReturn(Optional.of(item));

        assertThrows(SecurityException.class, () -> {
            cartService.removeCartItem(500, 1); // 1 is logged-in ID, 99 is owner
        });
    }

    @Test
    @DisplayName("CLEAR: Should call repository to delete all items for customer")
    void clearCart_Success() {
        when(cartItemRepository.deleteAllByCustomerId(1)).thenReturn(5);

        int deletedCount = cartService.clearCartByCustomer(1);

        assertEquals(5, deletedCount);
        verify(cartItemRepository).deleteAllByCustomerId(1);
    }
}