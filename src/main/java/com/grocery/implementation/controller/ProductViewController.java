package com.grocery.implementation.controller;



import com.grocery.implementation.model.ProductModel;
import com.grocery.implementation.service.ProductService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.stream.Collectors;

@Controller
//@RequestMapping("/products")
public class ProductViewController {

    private final ProductService productService;

    public ProductViewController(ProductService productService) {

        this.productService = productService;
    }

//    @GetMapping("/products")
//    public String products(Model model) {
//        model.addAttribute("products", productService.getAllProducts());
//        return "products"; // This maps to templates/products.html
//    }
    @GetMapping("/products")
    public String viewProducts(Model model) {

        // --- 🔑 Core Fix: Determine Authentication Status ---
        //model.addAttribute("products", productService.getAllProducts());
        List<ProductModel> rawProducts = productService.getAllProducts();
        List<ProductModel> safeProducts = rawProducts.stream()
                .filter(p -> p != null)
                .collect(Collectors.toList());

        model.addAttribute("products", safeProducts);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 1. Check if the authentication object is present and the user is authenticated,
        //    and is not the default 'anonymousUser' (which Spring Security uses for non-logged-in users).
        boolean isAuthenticated = (authentication != null &&
                authentication.isAuthenticated() &&
                !authentication.getPrincipal().equals("anonymousUser"));

        // 2. Add the boolean variable to the Model
        model.addAttribute("isAuthenticated", isAuthenticated);

        // 3. (Optional but recommended) Add the username if authenticated
        if (isAuthenticated) {
            model.addAttribute("username", authentication.getName());
        }
        // ----------------------------------------------------

        // ... (Add your products list here, e.g., model.addAttribute("products", productService.findAll());)

        return "products"; // The name of your Thymeleaf template
    }
    @GetMapping("/products/{id}")
    public String viewProductDetails(@PathVariable("id") Integer productId, Model model) {

        // 1. Fetch the product using the injected service
        ProductModel product = productService.getProductById(productId);

        // 2. Error handling
        if (product == null) {
            // Redirect to the list if product not found
            return "redirect:/products";
        }

        // 3. Add to Model
        model.addAttribute("product", product);

        // 4. Return the view
        return "product-details"; // Returns templates/product-details.html
    }
}
