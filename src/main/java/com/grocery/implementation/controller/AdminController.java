package com.grocery.implementation.controller;

import com.grocery.implementation.model.OrderModel;
import com.grocery.implementation.model.ProductModel;
import com.grocery.implementation.repository.OrderRepository;
import com.grocery.implementation.service.ProductService;
//import jakarta.persistence.criteria.Path;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import java.nio.file.Files;
import java.nio.file.*;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller // 🎯 Use @Controller for HTML views
@RequestMapping("/admin")
public class AdminController {

    private final ProductService productService;
    private final OrderRepository orderRepository;
    public AdminController(ProductService productService,OrderRepository orderRepository)
    {
        this.productService = productService;
        this.orderRepository= orderRepository;
    }

    // Opens the Admin Panel
    @GetMapping("/dashboard")
    public ModelAndView adminDashboard() {
        ModelAndView mav = new ModelAndView("admin/dashboard");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String adminName = "Admin"; // Default fallback
        if (auth != null && auth.isAuthenticated()) {
            adminName = auth.getName();
        }

        mav.addObject("adminName", adminName);

        mav.addObject("products", productService.getAllProducts());
        mav.addObject("product", new ProductModel());
        return mav;
    }

    @PostMapping("/products/save")
    public String saveProductUI(@ModelAttribute("product") ProductModel product
    ,@RequestParam("productImage") MultipartFile file) throws IOException {

        ProductModel savedProduct =productService.saveProduct(product);
        if (!file.isEmpty()) {
            String fileName = savedProduct.getProductId() + ".jpg";

            // Define path: You can use an absolute path or relative to project
            String uploadDir = "src/main/resources/static/images/products/";

            // Ensure directory exists
            Path uploadPath = (Path) Paths.get(uploadDir);
            if (!Files.exists((java.nio.file.Path) uploadPath)) {
                Files.createDirectories((java.nio.file.Path) uploadPath);
            }

            // 🎯 FIX: Files.copy accepts (InputStream, Path, CopyOption)
            try (InputStream inputStream = file.getInputStream()) {
                Path filePath = uploadPath.resolve(fileName);
                // 🎯 No casts needed. Java knows inputStream is a Stream and filePath is a Path.
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProductUI(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/products/edit/{id}")
    public ModelAndView editProductUI(@PathVariable Integer id) {
        ModelAndView mav = new ModelAndView("admin/dashboard");
        mav.addObject("product", productService.getProductById(id));
        mav.addObject("products", productService.getAllProducts());
        return mav;
    }
//    @GetMapping("/orders/details/{id}")
//    public String showOrderDetails(@PathVariable Integer id, Model model) {
//        OrderModel order = orderRepository.findById(id).orElse(null);
//        model.addAttribute("order", order);
//
//        // 🎯 This string MUST match your folder structure
//        return "admin/order-details"; // Re-check if this file exists!
//    }
@GetMapping("/orders/details/{id}")
public String showOrderDetails(@PathVariable Integer id, Model model) {
    OrderModel order = orderRepository.findById(id).orElse(null);

    // 🎯 Pull the specific address from the Map in OrderController
    // We use OrderController.checkoutAddresses because it is a static variable
    String specificAddress = OrderController.checkoutAddresses.get(id);

    // Fallback: if the map doesn't have it (e.g. after a restart), use the DB address
    if (specificAddress == null && order != null) {
        specificAddress = order.getCustomer().getAddress();
    }

    model.addAttribute("order", order);
    model.addAttribute("checkoutAddress", specificAddress); // Send the correct address string

    return "admin/order-details";
}
}