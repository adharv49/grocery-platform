package com.grocery.implementation.service;


import com.grocery.implementation.model.ProductModel;
import com.grocery.implementation.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<ProductModel> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public ProductModel getProductById(Integer productId) {
        return productRepository.findById(productId).orElse(null);
    }

    @Override
    public ProductModel saveProduct(ProductModel product) {
        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(Integer productId) {
        productRepository.deleteById(productId);
    }


    @Override
    public Optional<ProductModel> findById(Integer id) {
        // 🔑 Use the built-in JPA Repository findById method
        return productRepository.findById(id);
    }

    @Transactional
    public void increaseStock(Integer productId, int quantity) {
        productRepository.findById(productId).ifPresent(product -> {
            // Add the cancelled items back to stock
            product.setStockQuantity(product.getStockQuantity() + quantity);

            // If it was out of stock, make it available again
            if (product.getStockQuantity() > 0) {
                product.setStatus(ProductModel.Status.AVAILABLE);
            }
            productRepository.save(product);
        });
    }




    @Transactional
    public void decreaseStock(Integer productId, int quantity) {
        ProductModel product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStockQuantity() < quantity) {
            throw new IllegalStateException("Insufficient stock for product: " + product.getProductName());
        }

        int newStock = product.getStockQuantity() - quantity;
        product.setStockQuantity(newStock);

        // Automatically mark as OUT_OF_STOCK if it hits zero
        if (newStock == 0) {
            product.setStatus(ProductModel.Status.OUT_OF_STOCK);
        }

        productRepository.save(product);
    }
}

