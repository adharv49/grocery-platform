package com.grocery.implementation.controller;


import com.grocery.implementation.model.ProductModel;
import com.grocery.implementation.repository.DeliveryRepository;
import com.grocery.implementation.repository.OrderRepository;
import com.grocery.implementation.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;
    public ProductController(ProductService productService,OrderRepository orderRepository,DeliveryRepository deliveryRepository)
    {
        this.productService = productService;
        this.orderRepository = orderRepository;
        this.deliveryRepository=deliveryRepository;
    }

    @GetMapping
    public List<ProductModel> getAllProducts() {
        return productService.getAllProducts();
    }


    @GetMapping("/{productId}")
    public ResponseEntity<ProductModel> getProductById(@PathVariable Integer productId) {
        for(int i=0;i<50;i++){
            System.out.println("Controller hit for ID: " + productId);
        }

        ProductModel product = productService.getProductById(productId);

        return product != null ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
    }


    @PostMapping
    public ProductModel saveProduct(@RequestBody ProductModel product) {
        return productService.saveProduct(product);
    }

    @DeleteMapping("/{productId}")
    public String deleteProduct(@PathVariable Integer productId) {
        productService.deleteProduct(productId);
        return "Product deleted successfully!";
    }

    // Inside your ProductController.java (or relevant controller)
//    @GetMapping("/admin/dashboard")
//    public ModelAndView adminDashboard() {
//        // This will look for src/main/resources/templates/admin/dashboard.html
//        ModelAndView mav = new ModelAndView("admin/dashboard");
//        mav.addObject("products", productService.getAllProducts());
//        mav.addObject("product", new ProductModel()); // Empty object for the "Add" form
//        return mav;
//       // return new ModelAndView("admin/dashboard");
//    }
    // 2. Add/Update Product via Form (Admin UI)
//    @PostMapping("/admin/products/save")
//    public ModelAndView saveProductUI(@ModelAttribute("product") ProductModel product) {
//        productService.saveProduct(product);
//        return new ModelAndView("redirect:/admin/dashboard");
//    }
//    // 3. Delete Product via UI
//    // Note: We use GetMapping here because standard HTML links don't support DELETE easily
//    @GetMapping("/admin/products/delete/{id}")
//    public ModelAndView deleteProductUI(@PathVariable Integer id) {
//        productService.deleteProduct(id);
//        return new ModelAndView("redirect:/admin/dashboard");
//    }

    // 4. Edit Product (Fetches product data and reloads dashboard)
//    @GetMapping("/admin/products/edit/{id}")
//    public ModelAndView editProductUI(@PathVariable Integer id) {
//        ModelAndView mav = new ModelAndView("admin/dashboard");
//        mav.addObject("product", productService.getProductById(id));
//        mav.addObject("products", productService.getAllProducts());
//        return mav;
//    }
//



}
