package com.grocery.implementation.service;




import com.grocery.implementation.model.ProductModel;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<ProductModel> getAllProducts();
    ProductModel getProductById(Integer productId);
    ProductModel saveProduct(ProductModel product);
    void deleteProduct(Integer productId);
    Optional<ProductModel> findById(Integer id);
}
