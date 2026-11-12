package com.grocery.implementation.service;

import com.grocery.implementation.model.CartItemModel;
import com.grocery.implementation.model.ProductModel;
import com.grocery.implementation.model.UserModel;
import com.grocery.implementation.repository.CartItemRepository;
import com.grocery.implementation.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductService productService; // Assuming you have this
    private final UserService userService;
    private final UserRepository userRepository;// Assuming you have this

    public CartService(CartItemRepository cartItemRepository, ProductService productService, UserService userService,UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
        this.userService = userService;
        this.userRepository=userRepository;
    }

    // Inside CartService class
    private void checkPurchaseLimit(int quantity) {
        if (quantity > 5) {
            throw new IllegalArgumentException("Maximum purchase limit is 5 units per item.");
        }
    }
    // Change the return type to String to pass back the status/message
    @Transactional
    public String addProductToCart(Integer customerId, Integer productId, int quantity) {

        // 1. Fetch User and Product
//        if (quantity > 5) {
//            throw new IllegalArgumentException("Maximum purchase limit is 5 units per item.");
//        }
        checkPurchaseLimit(quantity);
        Optional<CartItemModel> existingItemOpt = cartItemRepository.findByCustomer_IdAndProduct_ProductId(customerId, productId);
        if (existingItemOpt.isPresent()) {
            CartItemModel item = existingItemOpt.get();
            // 🎯 FIX: Use item.getQuantity() (from Cart) not product.getStockQuantity()
            if ((item.getQuantity() + quantity) > 5) {
                return "LIMIT_EXCEEDED";
            }
        }
        UserModel customer = userService.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        ProductModel product = productService.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // 🔑 STOCK VALIDATION LOGIC 🔑
        if (product.getStatus().equals("OUT_OF_STOCK") || product.getStockQuantity() <= 0) {
            return "OUT_OF_STOCK"; // Return a status code
        }

        if (product.getStockQuantity() < quantity) {
            return "INSUFFICIENT_STOCK"; // Return a status code
        }
        // 🔑 END STOCK VALIDATION 🔑

        // 2. Check if the item already exists in the cart
        cartItemRepository.findByCustomer_IdAndProduct_ProductId(customerId, productId)
                .ifPresentOrElse(
                        // If present: update quantity
                        item -> {
                            int newTotalQuantity = item.getQuantity() + quantity;
                            if (product.getStockQuantity() < newTotalQuantity) {
                                // Secondary check: if combined quantity exceeds stock
                                throw new RuntimeException("Adding this quantity exceeds available stock.");
                            }
                            item.setQuantity(newTotalQuantity);
                            cartItemRepository.save(item);
                        },
                        // If not present: create new item
                        () -> {
                            CartItemModel newItem = new CartItemModel(customer, product, quantity);
                            cartItemRepository.save(newItem);
                        }
                );

        return "SUCCESS"; // Return a success code
    }
    // Method to get all cart items for display
    public List<CartItemModel> getCartItemsByCustomer(Integer customerId) {
        return cartItemRepository.findByCustomer_Id(customerId);
    }

    // Add remove/update methods here later...
    @Transactional
    public void removeCartItem(Integer cartItemId, Integer customerId) {
        // 1. Fetch the item to ensure it belongs to the current customer
        Optional<CartItemModel> itemOptional = cartItemRepository.findById(cartItemId);

        if (itemOptional.isPresent()) {
            CartItemModel item = itemOptional.get();

            // 2. Security Check: Only delete if the item belongs to the customer ID provided
            if (item.getCustomer().getId().equals(customerId)) {
                cartItemRepository.delete(item);
            } else {
                // Optional: Throw an exception or log a security warning if customerId mismatch occurs
                throw new SecurityException("Cart item ID " + cartItemId + " does not belong to customer " + customerId);
            }
        }
        // If the item doesn't exist, simply do nothing (safe approach)
    }
    @Transactional
    public void updateCartItemQuantity(Integer cartItemId, Integer customerId, Integer newQuantity) {

        if (newQuantity > 5) {
            throw new IllegalArgumentException("Maximum limit is 5 units per item.");
        }
        // 1. Fetch the item
        Optional<CartItemModel> itemOptional = cartItemRepository.findById(cartItemId);

        if (itemOptional.isPresent()) {
            CartItemModel item = itemOptional.get();

            // 2. Security Check: Ensure the item belongs to the customer
            if (!item.getCustomer().getId().equals(customerId)) {
                throw new SecurityException("Access denied: Cart item does not belong to this customer.");
            }

            // 3. Validation: If quantity is zero or less, treat it as a removal
            if (newQuantity <= 0) {
                cartItemRepository.delete(item);
            } else {
                // 4. Update and save
                item.setQuantity(newQuantity);
                cartItemRepository.save(item);
            }
        } else {
            // Optional: Throw an exception if item not found
            throw new RuntimeException("Cart item not found with ID: " + cartItemId);
        }
    }
    // Assuming you have a CartItemRepository for database interaction

    @Transactional
    public int clearCartByCustomer(Integer customerId) {
        // This method will perform the deletion and return the count of deleted records
        return cartItemRepository.deleteAllByCustomerId(customerId);
    }

    private BigDecimal calculateSubtotal(CartItemModel item) {
        BigDecimal itemPrice = item.getProduct().getPrice();
        BigDecimal quantity = new BigDecimal(item.getQuantity());
        return itemPrice.multiply(quantity);
    }

    @Transactional
    public BigDecimal getCartTotal(Integer customerId) {
        List<CartItemModel> cartItems = getCartItemsByCustomer(customerId); // Reusing your existing method

        BigDecimal total = BigDecimal.ZERO;

        for (CartItemModel item : cartItems) {
            // Calculate item subtotal (Price * Quantity)
//            BigDecimal itemPrice = item.getProduct().getPrice();
//            BigDecimal quantity = new BigDecimal(item.getQuantity());

            // item.getProduct().getPrice() should be BigDecimal for accuracy
//            BigDecimal subtotal = itemPrice.multiply(quantity);
//            total = total.add(subtotal);
            total = total.add(calculateSubtotal(item));
        }
        return total;
    }

    public List<CartItemModel> getCartItemsByCustomerId(Integer customerId) {
        // 1. Fetch the UserModel entity associated with the ID
        // Note: Replace this with your actual user fetching logic
        UserModel customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + customerId));

        // 2. Use the custom Spring Data JPA method to find items by the User entity
        return cartItemRepository.findByCustomer(customer);
    }
}