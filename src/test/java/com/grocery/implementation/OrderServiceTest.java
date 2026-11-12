package com.grocery.implementation;

//package com.grocery.implementation;

//package com.grocery.implementation.service;

import com.grocery.implementation.model.*;
import com.grocery.implementation.repository.CartItemRepository;
import com.grocery.implementation.repository.OrderRepository;
import com.grocery.implementation.repository.UserRepository;
import com.grocery.implementation.service.OrderService;
import com.grocery.implementation.service.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductServiceImpl productServiceImpl;

    @InjectMocks private OrderService orderService;

    private UserModel mockCustomer;
    private List<CartItemModel> mockCartItems;
    private ProductModel mockProduct;

    @BeforeEach
    void setUp() {
        mockCustomer = new UserModel();
        mockCustomer.setId(1);
        mockCustomer.setUsername("john_doe");
        mockCustomer.setAddress("123 Green Street");
        mockCustomer.setWalletBalance(new BigDecimal("500.00"));

        mockProduct = new ProductModel();
        mockProduct.setProductId(101);
        mockProduct.setProductName("Apple");
        mockProduct.setPrice(new BigDecimal("100.00"));

        CartItemModel item = new CartItemModel(mockCustomer, mockProduct, 2);
        mockCartItems = new ArrayList<>();
        mockCartItems.add(item);
    }

    @Test
    @DisplayName("CREATE ORDER: Success with full Wallet payment")
    void createOrder_FullWalletSuccess() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(mockCustomer));
        when(cartItemRepository.findByCustomer(mockCustomer)).thenReturn(mockCartItems);
        // Mock save to return the object passed to it
        when(orderRepository.save(any(OrderModel.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act (Total is 200, Wallet has 500)
        OrderModel result = orderService.createOrder(1, new CheckoutForm(), "UPI", true);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("300.00"), mockCustomer.getWalletBalance()); // 500 - 200
        assertEquals(OrderModel.OrderStatus.PLACED, result.getStatus());
        assertEquals("WALLET", result.getPayment().getPaymentMethod());
        verify(cartItemRepository).deleteAll(mockCartItems);
        verify(productServiceImpl).decreaseStock(101, 2);
    }

    @Test
    @DisplayName("CREATE ORDER: Fail when address is missing")
    void createOrder_NoAddressFail() {
        mockCustomer.setAddress(null);
        when(userRepository.findById(1)).thenReturn(Optional.of(mockCustomer));

        assertThrows(IllegalStateException.class, () ->
                orderService.createOrder(1, new CheckoutForm(), "COD", false)
        );
    }

    @Test
    @DisplayName("CREATE ORDER: Fail on partial wallet payment with COD")
    void createOrder_PartialWalletCODFail() {
        mockCustomer.setWalletBalance(new BigDecimal("50.00")); // Only covers 50 of 200
        when(userRepository.findById(1)).thenReturn(Optional.of(mockCustomer));
        when(cartItemRepository.findByCustomer(mockCustomer)).thenReturn(mockCartItems);

        assertThrows(IllegalStateException.class, () ->
                orderService.createOrder(1, new CheckoutForm(), "COD", true)
        );
    }

    @Test
    @DisplayName("CANCEL ORDER: Success with refund to wallet")
    void cancelOrder_WithRefund() {
        // Setup a mock order already placed with UPI
        OrderModel order = new OrderModel();
        order.setCustomer(mockCustomer);
        order.setStatus(OrderModel.OrderStatus.PLACED);
        order.setTotalAmount(new BigDecimal("200.00"));

        PaymentModel payment = new PaymentModel();
        payment.setPaymentMethod("UPI");
        payment.setPaymentStatus(PaymentModel.PaymentStatus.SUCCESS);
        payment.setPaymentAmount(new BigDecimal("200.00"));
        order.setPayment(payment);

        when(orderRepository.findById(123)).thenReturn(Optional.of(order));

        // Act
        orderService.cancelOrder(123, 1);

        // Assert
        assertEquals(OrderModel.OrderStatus.CANCELLED, order.getStatus());
        assertEquals(new BigDecimal("700.00"), mockCustomer.getWalletBalance()); // 500 + 200 refund
        assertEquals(PaymentModel.PaymentStatus.REFUNDED, payment.getPaymentStatus());
        verify(userRepository).save(mockCustomer);
    }

    @Test
    @DisplayName("ADMIN CANCEL: Confirm cancellation and mark COD as FAILED")
    void adminConfirmCancellation_COD() {
        OrderModel order = new OrderModel();
        order.setCustomer(mockCustomer);
        order.setTotalAmount(new BigDecimal("200.00"));

        PaymentModel payment = new PaymentModel();
        payment.setPaymentMethod("COD");
        payment.setPaymentStatus(PaymentModel.PaymentStatus.PENDING);
        order.setPayment(payment);

        when(orderRepository.findById(555)).thenReturn(Optional.of(order));

        // Act
        orderService.adminConfirmCancellation(555);

        // Assert
        assertEquals(OrderModel.OrderStatus.CANCELLED, order.getStatus());
        assertEquals(PaymentModel.PaymentStatus.FAILED, payment.getPaymentStatus());
        assertEquals(new BigDecimal("500.00"), mockCustomer.getWalletBalance()); // No refund for COD
    }
}
