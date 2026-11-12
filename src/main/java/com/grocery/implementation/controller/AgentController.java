package com.grocery.implementation.controller;

import com.grocery.implementation.model.DeliveryModel;
import com.grocery.implementation.model.OrderModel;
import com.grocery.implementation.model.PaymentModel;
import com.grocery.implementation.repository.DeliveryRepository;
import com.grocery.implementation.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AgentController {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired private DeliveryRepository deliveryRepository;
    @GetMapping("/agent/tasks")
    public ModelAndView viewAgentTasks(Authentication authentication) {
        String currentAgentName = authentication.getName(); // Gets logged-in username
        ModelAndView mav = new ModelAndView("agent/tasks"); // refers to tasks.html

        // Fetch all orders
        List<OrderModel> allOrders = orderRepository.findAll();
        List<DeliveryModel> all = deliveryRepository.findAll();
        // Filter orders where the delivery info contains the agent's name
        // This is necessary because we stored agent name in the timeSlot string
        List<DeliveryModel> myTasks = all.stream()
                .filter(d -> d.getTimeSlot() != null && d.getTimeSlot().contains(currentAgentName))
                .filter(d -> !d.getDeliveryStatus().name().equals("DELIVERED"))
                .filter(d -> !d.getDeliveryStatus().name().equals("CANCELLED"))
                .collect(Collectors.toList());

        mav.addObject("deliveries", myTasks);
        mav.addObject("agentName", currentAgentName);
        return mav;
       // return mav;
    }

    @GetMapping("/agent/status")
    public String updateDeliveryStatus(@RequestParam Integer id, @RequestParam String s) {
        DeliveryModel delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        // Update Delivery Status
       // delivery.setDeliveryStatus(DeliveryModel.DeliveryStatus.valueOf(s));
        DeliveryModel.DeliveryStatus newStatus = DeliveryModel.DeliveryStatus.valueOf(s);
        delivery.setDeliveryStatus(newStatus);

        if (newStatus == DeliveryModel.DeliveryStatus.DELIVERED) {
            delivery.setDeliveryDate(LocalDate.now());
        }
        if ("DELIVERED".equals(s)) {
            delivery.setDeliveryDate(LocalDate.now());
        }
        deliveryRepository.save(delivery);

        // Also update the parent Order status to keep them in sync
        OrderModel order = delivery.getOrder();
        order.setStatus(OrderModel.OrderStatus.valueOf(s));
        if (newStatus == DeliveryModel.DeliveryStatus.DELIVERED) {
            if (order.getPayment() != null) {
                // Mark payment as finished (This handles COD collection)
                order.getPayment().setPaymentStatus(PaymentModel.PaymentStatus.SUCCESS);
                order.getPayment().setPaymentDate(LocalDate.now());
            }
        }
        orderRepository.save(order);

        return "redirect:/agent/tasks";
    }
    @GetMapping("/agent/history")
    public String viewAgentHistory(Model model, Principal principal) {
        String agentName = principal.getName();

        // 1. Fetch all deliveries from the DB
        List<DeliveryModel> allDeliveries = deliveryRepository.findAll();

        // 2. Filter for those that belong to THIS agent and are already DELIVERED
        List<DeliveryModel> history = allDeliveries.stream()
                .filter(d -> d.getDeliveryStatus() == DeliveryModel.DeliveryStatus.DELIVERED)
                .filter(d -> d.getTimeSlot() != null && d.getTimeSlot().contains(agentName))
                .sorted((a, b) -> b.getDeliveryDate().compareTo(a.getDeliveryDate())) // Newest first
                .collect(Collectors.toList());

        model.addAttribute("history", history);
        model.addAttribute("agentName", agentName);

        return "agent/history";
    }


    @GetMapping("/agent/cancelled")
    public String viewCancelledTasks(Model model, Principal principal) {
        String agentName = principal.getName();

        // 1. Fetch all deliveries
        List<DeliveryModel> allDeliveries = deliveryRepository.findAll();

        // 2. Filter specifically for CANCELLED tasks belonging to this agent
        List<DeliveryModel> cancelledTasks = allDeliveries.stream()
                .filter(d -> d.getDeliveryStatus() == DeliveryModel.DeliveryStatus.CANCELLED)
                .filter(d -> d.getTimeSlot() != null && d.getTimeSlot().contains(agentName))
                .sorted((a, b) -> {
                    // Handle null dates to avoid crashes
                    if (a.getDeliveryDate() == null || b.getDeliveryDate() == null) return 0;
                    return b.getDeliveryDate().compareTo(a.getDeliveryDate());
                })
                .collect(Collectors.toList());

        model.addAttribute("cancelledDeliveries", cancelledTasks);
        model.addAttribute("agentName", agentName);

        // This points to agent/cancelled-history.html or agent/cancelled.html
        return "agent/cancelled-history";
    }
}