package com.grocery.implementation.service;

import com.grocery.implementation.model.*;
import com.grocery.implementation.repository.*;
import com.grocery.implementation.model.*;
import com.grocery.implementation.repository.CartItemRepository;
import com.grocery.implementation.repository.OrderRepository;
import com.grocery.implementation.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductServiceImpl productServiceImpl;
    // Note: If you use Cascading in OrderModel, you don't strictly need
    // to inject OrderItem, Delivery, or Payment repositories here.


    public OrderService(OrderRepository orderRepository, CartItemRepository cartItemRepository, UserRepository userRepository, ProductServiceImpl productServiceImpl) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productServiceImpl=productServiceImpl;
    }

    /**
     * Creates a complete Order resource including Delivery, Payment, and Items.
     */
    @Transactional
    public OrderModel createOrder(Integer customerId, CheckoutForm checkoutForm,String paymentMethod, boolean useWallet) {
        // 1. Validate Customer
        UserModel customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        if (customer.getAddress() == null || customer.getAddress().trim().isEmpty()) {
            throw new IllegalStateException("Order failed: Please set a delivery address in your profile.");
        }
        // 2. Fetch Cart Items
        List<CartItemModel> cartItems = cartItemRepository.findByCustomer(customer);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cannot create order: Your cart is empty.");
        }

        //
        // 2. 🎯 Wallet Logic
        //List<CartItemModel> items = cartService.getCartItemsByCustomerId(customerId);
        // 3. Calculate Total
        BigDecimal total = cartItems.stream()
                .map(i -> i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remainingAmount = total;
        if (useWallet && customer.getWalletBalance().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal balance = customer.getWalletBalance();

            if (balance.compareTo(total) >= 0) {
                // Wallet covers everything
                customer.setWalletBalance(balance.subtract(total));
               remainingAmount = BigDecimal.ZERO;
                paymentMethod = "WALLET"; // Fully paid by wallet
            } else {
                // Wallet covers part of it
                if ("COD".equalsIgnoreCase(paymentMethod)) {
                    throw new IllegalStateException("Partial wallet payments are not allowed with Cash on Delivery. Please use UPI or Card.");
                }
                remainingAmount = total.subtract(balance);
                customer.setWalletBalance(BigDecimal.ZERO);
                // Remaining 'total' will be paid by COD/UPI
            }
            userRepository.save(customer);
        }



        // 4. Initialize Order Object
        OrderModel newOrder = new OrderModel();
        newOrder.setCustomer(customer);
        newOrder.setOrderDate(LocalDate.now());
        newOrder.setTotalAmount(total);
        newOrder.setStatus(OrderModel.OrderStatus.PLACED);

        // 5. Map Cart Items to Order Items (Snapshots)
        for (CartItemModel cartItem : cartItems) {
            productServiceImpl.decreaseStock(cartItem.getProduct().getProductId(), cartItem.getQuantity());
            OrderItemModel orderItem = new OrderItemModel();
            orderItem.setProductId(cartItem.getProduct().getProductId());
            orderItem.setProductName(cartItem.getProduct().getProductName());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getProduct().getPrice());

            // Link the item to the order (This handles the foreign key)
            newOrder.addOrderItem(orderItem);
        }

        // 6. Create and Link Delivery Record
        DeliveryModel delivery = new DeliveryModel();
        delivery.setOrder(newOrder);
        delivery.setDeliveryDate(LocalDate.now().plusDays(3));
        delivery.setTimeSlot("10:00 AM - 02:00 PM");
        delivery.setDeliveryStatus(DeliveryModel.DeliveryStatus.SCHEDULED);
     //   newOrder.setDelivery(delivery);

        // 7. Create and Link Payment Record
        PaymentModel payment = new PaymentModel();
        payment.setOrder(newOrder);

        payment.setPaymentAmount(remainingAmount);


        payment.setPaymentAmount(total);
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentStatus(PaymentModel.PaymentStatus.PENDING);
        payment.setPaymentMethod(paymentMethod);

        // Logic: If it's not COD, mark as COMPLETED (assuming frontend validated it)
        if ("COD".equalsIgnoreCase(paymentMethod)) {
            payment.setPaymentStatus(PaymentModel.PaymentStatus.PENDING);
        } else {
            payment.setPaymentStatus(PaymentModel.PaymentStatus.SUCCESS);
        }
        newOrder.setPayment(payment);

        // 8. PERSIST EVERYTHING
        // Because of CascadeType.ALL, saving the Order saves Items, Delivery, and Payment
        newOrder = orderRepository.save(newOrder);

        // 9. CLEAR CART
        cartItemRepository.deleteAll(cartItems);

        return newOrder;
    }

    @Transactional(readOnly = true)
    public OrderModel getOrderDetails(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
    }

    @Transactional
    public void cancelOrder(Integer orderId, Integer customerId) {
        OrderModel order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Security check
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new SecurityException("Unauthorized access");
        }

        // Only PLACED orders can be cancelled
        if (order.getStatus() != OrderModel.OrderStatus.PLACED) {
            throw new IllegalStateException("Cannot cancel order in " + order.getStatus() + " status.");
        }
        for (OrderItemModel item : order.getOrderItems()) {
            productServiceImpl.increaseStock(item.getProductId(), item.getQuantity());
        }

        order.setStatus(OrderModel.OrderStatus.CANCELLED);
        orderRepository.save(order);
        //refund
        PaymentModel payment = order.getPayment();
        String method = payment.getPaymentMethod();
        PaymentModel.PaymentStatus status = payment.getPaymentStatus();
        if (!"COD".equalsIgnoreCase(method) && status == PaymentModel.PaymentStatus.SUCCESS) {
            UserModel customer = order.getCustomer();
            BigDecimal refundAmount = payment.getPaymentAmount();

            BigDecimal currentBalance = (customer.getWalletBalance() == null)
                    ? BigDecimal.ZERO : customer.getWalletBalance();

            customer.setWalletBalance(currentBalance.add(refundAmount));
            userRepository.save(customer);

            payment.setPaymentStatus(PaymentModel.PaymentStatus.REFUNDED);
        }
    }


    @Transactional(readOnly = true)
    public List<OrderModel> getOrdersByCustomerId(Integer customerId) {
       // return orderRepository.findByCustomer_IdOrderByOrderDateDesc(customerId);
        return orderRepository.findByCustomer_IdOrderByOrderDateDesc(customerId);
    }






    // 1. Customer calls this for SCHEDULED orders
    @Transactional
    public void requestCancellation(Integer orderId, Integer customerId) {
        OrderModel order = orderRepository.findById(orderId).get();
        if (!order.getCustomer().getId().equals(customerId)) throw new SecurityException();

        order.setStatus(OrderModel.OrderStatus.CANCELLATION_REQUESTED);
        orderRepository.save(order);
    }

    // 2. Admin calls this to finalize
//    @Transactional
//    public void adminConfirmCancellation(Integer orderId) {
//        OrderModel order = orderRepository.findById(orderId).get();
//
//        // Restore Stock
//        for (OrderItemModel item : order.getOrderItems()) {
//            productServiceImpl.increaseStock(item.getProductId(), item.getQuantity());
//        }
//
//        order.setStatus(OrderModel.OrderStatus.CANCELLED);
//        orderRepository.save(order);
//    }

    @Transactional
    public void adminConfirmCancellation(Integer orderId) {
        OrderModel order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 1. Restore Stock
        for (OrderItemModel item : order.getOrderItems()) {
            productServiceImpl.increaseStock(item.getProductId(), item.getQuantity());
        }

        // 2. Handle Refund Logic
        PaymentModel payment = order.getPayment();
        if (payment != null) {
            // Only refund if payment was actually completed (Digital payments)
            String method = payment.getPaymentMethod(); // "COD", "UPI", "CARD"
            PaymentModel.PaymentStatus status = payment.getPaymentStatus();

            // Check if it's an online payment that was successful
            if (!"COD".equalsIgnoreCase(method) && status == PaymentModel.PaymentStatus.SUCCESS) {
                // 🎯 Add money to user's wallet
                UserModel customer = order.getCustomer();
                BigDecimal refundAmount = order.getTotalAmount();

                BigDecimal currentBalance = customer.getWalletBalance();
                if (currentBalance == null) currentBalance = BigDecimal.ZERO;

                customer.setWalletBalance(currentBalance.add(refundAmount));
                userRepository.save(customer);
                // 🎯 THIS IS THE FIX: Only UPI/CARD gets marked as REFUNDED
                payment.setPaymentStatus(PaymentModel.PaymentStatus.REFUNDED);
            }
            else {
                // 🎯 FOR COD: Whether it was PENDING or SUCCESS, mark as FAILED
                // because no digital money needs to be moved back.
                payment.setPaymentStatus(PaymentModel.PaymentStatus.FAILED);
            }
        }

        // 3. Update Order Status
        if (order.getDeliveries() != null) {
            for (DeliveryModel delivery : order.getDeliveries()) {
                // Ensure CANCELLED exists in your DeliveryStatus Enum
                delivery.setDeliveryStatus(DeliveryModel.DeliveryStatus.CANCELLED);
            }
        }
        order.setStatus(OrderModel.OrderStatus.CANCELLED);
        orderRepository.save(order);

    }
}