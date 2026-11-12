package com.grocery.implementation.repository;



import com.grocery.implementation.model.CartItemModel;
import com.grocery.implementation.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItemModel, Integer> {

    // Custom method to find all items for a specific customer
    List<CartItemModel> findByCustomer_Id(Integer customerId);

    // 🔑 CORRECTED: findByCustomer**Id**AndProduct**Id** - uses the 'id' field for both relationships.
    Optional<CartItemModel> findByCustomer_IdAndProduct_ProductId(Integer customerId, Integer productId);
    //public int clearCartByCustomer(Integer customerId);

    int deleteAllByCustomerId(Integer customerId);
    // Example method signature you need in CartService
    public List<CartItemModel> getCartItemsByCustomerId(Integer customerId);
    List<CartItemModel> findByCustomer(UserModel customer);
    void deleteByCustomer(UserModel customer);

}