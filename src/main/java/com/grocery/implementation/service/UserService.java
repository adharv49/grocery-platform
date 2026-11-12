package com.grocery.implementation.service;

import com.grocery.implementation.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.grocery.implementation.repository.UserRepository;

import java.util.Optional;

@Service
public class UserService {
    //@Autowired
	private final UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
    private void assignDynamicRole(UserModel user) {
        String lowerName = user.getUsername().toLowerCase();
        if (lowerName.startsWith("admin")) {
            user.setRole("ADMIN");
        } else if (lowerName.startsWith("agent")) {
            user.setRole("DELIVERY_AGENT");
        } else {
            user.setRole("CUSTOMER");
        }
    }
	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
    //@Override
    public void createRest(UserModel u) {
        u.setPassword(passwordEncoder.encode(u.getPassword()));
        assignDynamicRole(u);
        userRepository.save(u);
    }
	public String registerUser(String username, String email,String password, String address) {

        if (userRepository.findByUsername(username)!=null) {
            return "User already exists!";
        }
        if (userRepository.findByEmail(email)!=null) {
            return "Email already registered!";
        }
        UserModel userModel = new UserModel();
		userModel.setUsername(username);
        userModel.setEmail(email);
		userModel.setPassword(passwordEncoder.encode(password));
		//userModel.setRole("CUSTOMER");
        userModel.setAddress(address);
        assignDynamicRole(userModel);
        //userModel.setRole(role);
		userRepository.save(userModel);
        return "success";
	}

    public boolean validateUser(String username, String password) {
        UserModel userModel = userRepository.findByUsername(username);
        if (userModel == null) {
            return false;
        }
        return passwordEncoder.matches(password, userModel.getPassword());
    }

    public boolean isUsernameValid(String username)
    {
        return userRepository.findByUsername(username) != null;
    }


    public Optional<UserModel> findById(Integer id) {
        // 🔑 Use the built-in JPA Repository findById method
        return userRepository.findById(id);
    }
    public UserDetails updateUserDetails(String oldUsername, String newUsername, String newAddress) {
        UserModel user = userRepository.findByUsername(oldUsername);
        if (user != null) {
            user.setUsername(newUsername);
            user.setAddress(newAddress);
            userRepository.save(user);
        }
        return new CustomUserDetails(user);
    }
}
