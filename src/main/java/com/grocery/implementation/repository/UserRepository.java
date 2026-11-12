package com.grocery.implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grocery.implementation.model.UserModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Integer> {

	UserModel findByUsername(String username);
    UserModel findByEmail(String email);
    Optional<UserModel> findById(Integer id);
    List<UserModel> findByRole(String role);
    //Optional<UserModel> findByUsername(String username);

}
