package com.grocery.implementation.controller;

import com.grocery.implementation.model.CartItemModel;
import com.grocery.implementation.model.UserModel;
import com.grocery.implementation.repository.UserRepository;
import com.grocery.implementation.service.CartService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    public CartController(CartService cartService, UserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }



    private Integer getCurrentCustomerId(Authentication authentication) {
        // Placeholder: Ensure this matches your login logic
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

    // --- 1. Add Item to Cart ---
    @PostMapping("/add")
    public String addItemToCart(@RequestParam("productId") Integer productId,
                                @RequestParam("quantity") int quantity,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {

        Integer customerId = getCurrentCustomerId(authentication);

        if (quantity < 1) {
            redirectAttributes.addFlashAttribute("cartMessage", "Quantity must be at least 1.");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/products";
        }

        String status = cartService.addProductToCart(customerId, productId, quantity);
        //String result = cartService.addProductToCart(customerId, productId, quantity);
        if (status.equals("OUT_OF_STOCK")) {
            redirectAttributes.addFlashAttribute("cartMessage", "Item is out of stock.");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        else if ("LIMIT_EXCEEDED".equals(status)){
            // 🎯 THIS IS THE FIX: Set an error message when limit is hit
            redirectAttributes.addFlashAttribute("cartMessage", "Error: Maximum limit is 5 units per item.");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        else if (status.equals("INSUFFICIENT_STOCK")) {
            redirectAttributes.addFlashAttribute("cartMessage", "Not enough stock available.");
            redirectAttributes.addFlashAttribute("messageType", "error");
        } else {
            redirectAttributes.addFlashAttribute("cartMessage", "Added to cart!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        }

        return "redirect:/products";
    }

    // --- 2. View Cart ---
    @GetMapping
    public String viewCart(Model model, Authentication authentication) {
        Integer customerId = getCurrentCustomerId(authentication);

        List<CartItemModel> cartItems = cartService.getCartItemsByCustomer(customerId);

        // 🔑 Ensure this returns BigDecimal from the Service
        BigDecimal cartTotal = cartService.getCartTotal(customerId);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartTotal", cartTotal != null ? cartTotal : BigDecimal.ZERO);
        return "cart";
    }

    // --- 3. Update Quantity ---
    @PostMapping("/update")
    public String updateItemQuantity(@RequestParam("cartItemId") Integer cartItemId,
                                     @RequestParam("quantity") Integer quantity,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        Integer customerId = getCurrentCustomerId(authentication);
        try {
            cartService.updateCartItemQuantity(cartItemId, customerId, quantity);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("cartMessage", "Update failed: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/cart";
    }

    // --- 4. Remove Item ---
    @PostMapping("/remove")
    public String removeItemFromCart(@RequestParam("cartItemId") Integer cartItemId,
                                     Authentication authentication) {
        Integer customerId = getCurrentCustomerId(authentication);
        cartService.removeCartItem(cartItemId, customerId);
        return "redirect:/cart";
    }

    // --- 5. Clear Cart ---
    @PostMapping("/clear")
    public String clearCart(Authentication authentication, RedirectAttributes redirectAttributes) {
        Integer customerId = getCurrentCustomerId(authentication);
        int itemsCleared = cartService.clearCartByCustomer(customerId);
        redirectAttributes.addFlashAttribute("cartMessage", itemsCleared + " items removed.");
        redirectAttributes.addFlashAttribute("messageType", "info");
        return "redirect:/cart";
    }

    // --- 6. Buy Now (Direct to Checkout) ---
    @PostMapping("/buyNow")
    public String buyNow(@RequestParam("productId") Integer productId,
                         @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {

        Integer customerId = getCurrentCustomerId(authentication);
        String status = cartService.addProductToCart(customerId, productId, quantity);

        if (status.equals("SUCCESS")) {
            // 🚀 IMPROVEMENT: Skip the cart page and go straight to checkout form
            return "redirect:/orders/checkout";
        } else {
            redirectAttributes.addFlashAttribute("cartMessage", "Cannot buy now: " + status);
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/products";
        }
    }
}