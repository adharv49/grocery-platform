package com.grocery.implementation.aspect;

import com.grocery.implementation.model.OrderModel;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class OrderServiceAspect {

    // Matches the createOrder method in your OrderService
    @Before(value = "execution(* com.grocery.implementation.service.OrderService.createOrder(..)) && args(customerId, checkoutForm, paymentMethod, useWallet)")
    public void beforeCreateOrder(JoinPoint joinPoint, Integer customerId, Object checkoutForm, String paymentMethod, boolean useWallet) {
        System.out.println("\n--- AOP: TRANSACTION MONITORING START ---");
        System.out.println("Processing Order for Customer ID: " + customerId);
        System.out.println("Payment Method Selected: " + paymentMethod);
        System.out.println("Wallet Applied: " + (useWallet ? "YES" : "NO"));
        System.out.println("Executing Method: " + joinPoint.getSignature().getName());
    }

    // Capture the result only if the method finishes successfully (Database record created)
    @AfterReturning(value = "execution(* com.grocery.implementation.service.OrderService.createOrder(..))", returning = "result")
    public void afterSuccessfulOrder(JoinPoint joinPoint, Object result) {
        if (result instanceof OrderModel) {
            OrderModel order = (OrderModel) result;
            System.out.println("--- AOP: ORDER PLACED SUCCESSFULLY ---");
            System.out.println("Generated Order ID: " + order.getOrderId());
            System.out.println("Total Amount Deducted: ₹" + order.getTotalAmount());
            System.out.println("Status Set To: " + order.getStatus());
            System.out.println("---------------------------------------\n");
        }
    }
}