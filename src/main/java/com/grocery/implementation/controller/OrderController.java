package com.grocery.implementation.controller;

import com.grocery.implementation.model.*;
import com.grocery.implementation.repository.DeliveryRepository;
import com.grocery.implementation.repository.OrderRepository;
import com.grocery.implementation.repository.UserRepository;
import com.grocery.implementation.service.CartService;
import com.grocery.implementation.service.InvoiceService;
import com.grocery.implementation.service.OrderService;
import com.grocery.implementation.service.ProductService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.math.BigDecimal; // 🔑 REQUIRED for price precision
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
//@RequestMapping("/orders")
public class OrderController {

    private final CartService cartService;
    private final OrderService orderService;
    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;

    public OrderController(CartService cartService, OrderService orderService, ProductService productService, OrderRepository orderRepository, DeliveryRepository deliveryRepository,UserRepository userRepository) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.productService = productService;
        this.orderRepository = orderRepository;
        this.deliveryRepository = deliveryRepository;
        this.userRepository=userRepository;
    }


    private Integer getCurrentCustomerId(Authentication authentication) {

        if (authentication == null) {
            System.out.println("DEBUG: Authentication is null");
            return null;
        }

        String username = authentication.getName();
        System.out.println("DEBUG: Logged in name is: " +username);
        UserModel user = userRepository.findByUsername(username);
        System.out.println("DEBUG: Logged in id is: " +user.getId());
        return user.getId();

    }

    /**
     * 1. Displays the checkout page with cart items and the total sum.
     */
    @GetMapping("/orders/checkout")
    public String viewCheckoutForm(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        Integer customerId = getCurrentCustomerId(authentication);
        //Integer customerId = user.getId();
        UserModel user = userRepository.findById(customerId).get();
        List<CartItemModel> cartItems = cartService.getCartItemsByCustomerId(customerId);
        System.out.println("DEBUG: Cart size for User " + customerId + " is: " + cartItems.size());
        if (cartItems == null || cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Your cart is empty.");
            return "redirect:/products";
        }
        if (user.getAddress() == null || user.getAddress().trim().isEmpty()) {
            model.addAttribute("addressMissing", true);
        } else {
            model.addAttribute("savedAddress", user.getAddress());
        }
        // 🔑 Updated to use BigDecimal for financial accuracy 🔑
        BigDecimal totalAmount = cartItems.stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("userBalance", user.getWalletBalance());
        model.addAttribute("checkoutForm", new CheckoutForm());

        return "checkout";
    }

    /**
     * 2. Processes the checkout form and redirects to confirmation.
     */
    //@PostMapping("/orders/checkout")
    public static Map<Integer, String> checkoutAddresses = new java.util.concurrent.ConcurrentHashMap<>();
    @PostMapping("/orders/place")
    public String createOrder(@ModelAttribute CheckoutForm checkoutForm,
                              @RequestParam(value = "useWallet", required = false) boolean useWallet,
                              @RequestParam(value = "paymentMethod", defaultValue = "COD") String paymentMethod,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes, Principal principal) {
        String username = authentication.getName();
        String targetAddress = checkoutForm.getAddressLine1();


        Integer customerId = getCurrentCustomerId(authentication);

        try {

            OrderModel newOrder = orderService.createOrder(customerId, checkoutForm, paymentMethod,useWallet);
            if (newOrder != null && newOrder.getOrderId() != null) {
                checkoutAddresses.put(newOrder.getOrderId(), targetAddress);
            }
            redirectAttributes.addFlashAttribute("orderMessage", "Thank you! Your order has been placed.");
            return "redirect:/orders/" + newOrder.getOrderId();

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            return "redirect:/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Checkout error: " + e.getMessage());
            return "redirect:/products";
        }
    }

    /**
     * 3. Displays the confirmation page for a specific order.
     */
    @GetMapping("/orders/{id}")
    public String viewOrderDetails(@PathVariable("id") Integer orderId, Model model,Authentication authentication) {
        OrderModel order = orderService.getOrderDetails(orderId);
        String address = null;
        if (order.getDeliveries() != null && !order.getDeliveries().isEmpty()) {
            address = order.getDeliveries().get(0).getDeliveryAddress();
        }

        // 2. If not in DB, check the temporary Map (Memory)
        if (address == null) {
            address = checkoutAddresses.get(orderId);
        }

        // 3. Fallback to the default profile address
        if (address == null) {
            address = order.getCustomer().getAddress();
        }
//        String address = checkoutAddresses.get(orderId);
//
//        if (address == null) {
//            address = order.getCustomer().getAddress();
//        }
        model.addAttribute("order", order);
        model.addAttribute("checkoutAddress", address);
        //
        //OrderModel order = orderService.getOrderDetails(orderId);
        model.addAttribute("userBalance", order.getCustomer().getWalletBalance());

        return "order-confirmation";
    }

  //  import jakarta.servlet.http.HttpServletResponse; // 🔑
    @Autowired
    private InvoiceService invoiceService;
    @GetMapping("/orders/{id}/download")
    public void downloadInvoice(@PathVariable("id") Integer orderId, HttpServletResponse response) throws IOException {
        OrderModel order = orderService.getOrderDetails(orderId);
        String invoiceAddress = null;

        // 1. Check if order has a scheduled delivery (Permanent Address 2 in DB)
        if (order.getDeliveries() != null && !order.getDeliveries().isEmpty()) {
            invoiceAddress = order.getDeliveries().get(0).getDeliveryAddress();
        }

        // 2. If not scheduled, check the temporary Map (Address 2 in memory)
        if (invoiceAddress == null && checkoutAddresses.containsKey(orderId)) {
            invoiceAddress = checkoutAddresses.get(orderId);
        }
        if (invoiceAddress == null) {
            invoiceAddress = order.getCustomer().getAddress();
        }
        // Set headers for PDF download
        response.setContentType("application/pdf");
//        String address = checkoutAddresses.get(orderId);
//       // String address = order.getCustomer().getAddress();
//
//        if (address == null) {
//            address = order.getCustomer().getAddress();
//        }
        response.setHeader("Content-Disposition", "attachment; filename=Invoice_" + orderId + ".pdf");

        invoiceService.generateInvoice(order,invoiceAddress,response);
    }
    /**
     * 4. Displays the list of all previous orders for the current user.
     */
    @GetMapping("/orders/history")
    public String viewOrderHistory(Model model, Authentication authentication) {
        Integer customerId = getCurrentCustomerId(authentication);

        // Fetch all orders from the service
        List<OrderModel> orders = orderService.getOrdersByCustomerId(customerId);

        model.addAttribute("orders", orders);
        return "order-history";
    }
    @GetMapping("/admin/orders")
    public ModelAndView viewOrders() {
        ModelAndView mav = new ModelAndView("admin/orders");
        // Fetch all orders from your repository
        mav.addObject("orders", orderRepository.findAll());
        List<UserModel> agents = userRepository.findByRole("delivery_agent");
        mav.addObject("agents", agents);
        mav.addObject("addressMap", checkoutAddresses);

        return mav;
        //return mav;
    }
    @GetMapping("/admin/orders/status")
    public ModelAndView updateOrderStatus(@RequestParam Integer id, @RequestParam String s) {
        // Find the order
        OrderModel order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order Id:" + id));

        // Update the status
        order.setStatus(OrderModel.OrderStatus.valueOf(s));

        // Save back to DB
        orderRepository.save(order);

        // Refresh the page
        return new ModelAndView("redirect:/admin/orders");
    }
    @PostMapping("/admin/orders/assign-delivery")
    public String assignToDelivery(@RequestParam Integer orderId,@RequestParam String agentName, @RequestParam String timeSlot,RedirectAttributes redirectAttributes) {
        LocalDate deliveryDate = LocalDate.now().plusDays(1);
        String slotIdentifier = agentName + " | " + timeSlot;
        long currentLoad = deliveryRepository.countActiveOrdersForAgentInSlot(slotIdentifier, deliveryDate);//countByTimeSlotAndDeliveryDate(agentName + " | " + timeSlot, deliveryDate);
        System.out.println("DEBUG:Before Agent " + agentName + " has " + currentLoad + " orders in slot " + timeSlot);
        if (currentLoad >= 5) {
            redirectAttributes.addFlashAttribute("message", "Error: Agent " + agentName + " is fully booked (5/5) for " + timeSlot);
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/admin/orders";
        }
        // assign
        OrderModel order = orderRepository.findById(orderId).get();
        DeliveryModel delivery = new DeliveryModel();
        delivery.setOrder(order);
        String finalAddress = checkoutAddresses.get(orderId);
        if (finalAddress == null) {
            finalAddress = order.getCustomer().getAddress(); // Fallback
        }
        delivery.setDeliveryAddress(finalAddress);

        //delivery.setAgentName(agentName); // 🎯 Saved to its own column
        delivery.setTimeSlot(agentName + " | " + timeSlot);  // 🎯 Saved to its own column
        delivery.setDeliveryStatus(DeliveryModel.DeliveryStatus.SCHEDULED);
        delivery.setDeliveryDate(deliveryDate);
        deliveryRepository.save(delivery);

        order.setStatus(OrderModel.OrderStatus.SCHEDULED);
        orderRepository.save(order);
        long afterLoad = deliveryRepository.countActiveOrdersForAgentInSlot(slotIdentifier, deliveryDate);
        System.out.println("DEBUG:after Agent " + agentName + " has " + afterLoad + " orders in slot " + timeSlot);
        //checkoutAddresses.remove(orderId);
        redirectAttributes.addFlashAttribute("message", "Success: Assigned to " + agentName);
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/admin/orders";


    }

    @PostMapping("/orders/cancel")
    public String cancelOrder(@RequestParam("orderId") Integer orderId,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            Integer customerId = getCurrentCustomerId(authentication);
            orderService.cancelOrder(orderId, customerId);

            redirectAttributes.addFlashAttribute("orderMessage", "Order #" + orderId + " has been cancelled successfully.");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("orderMessage", "Cancellation failed: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/orders/history";
    }

    // For the Customer
    @PostMapping("/orders/request-cancel")
    public String requestCancel(@RequestParam Integer orderId, Authentication auth) {
        orderService.requestCancellation(orderId, getCurrentCustomerId(auth));
        return "redirect:/orders/history";
    }

    // For the Admin
    @PostMapping("/admin/orders/confirm-cancel")
    public String adminCancel(@RequestParam Integer orderId, RedirectAttributes redirectAttributes) {
        //orderService.adminConfirmCancellation(orderId);
        try {
            //orderService.adminConfirmCancellation(orderId);
            OrderModel order = orderService.getOrderDetails(orderId);
            String paymentMethod = (order.getPayment() != null) ? order.getPayment().getPaymentMethod() : "COD";

            // 2. Execute the cancellation logic (Stock, Delivery, Payment status)
            orderService.adminConfirmCancellation(orderId);

            // 3. Customize the success message
            String successMsg = "Order #" + orderId + " has been cancelled.";

            // 🎯 Logic: Add refund statement only if NOT COD
            if (!"COD".equalsIgnoreCase(paymentMethod)) {
                successMsg += " Refund of ₹" + order.getTotalAmount() + " has been initiated to the user's account.";
            }
            redirectAttributes.addFlashAttribute("message", successMsg);
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/orders";
    }
}