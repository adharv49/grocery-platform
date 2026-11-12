package com.grocery.implementation;

//package com.grocery.implementation.service;

import com.grocery.implementation.model.UserModel;
import com.grocery.implementation.repository.UserRepository;
import com.grocery.implementation.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserModel mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new UserModel();
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@example.com");
        mockUser.setPassword("plainPassword");
    }

    @Test
    @DisplayName("ROLE: Should assign ADMIN role if username starts with admin")
    void registerUser_AssignAdminRole() {
        // Arrange
        when(userRepository.findByUsername("admin_bob")).thenReturn(null);
        when(passwordEncoder.encode("secret")).thenReturn("hashed_secret");

        // Act
        String result = userService.registerUser("admin_bob", "admin@mart.com", "secret", "HQ");

        // Assert
        assertEquals("success", result);
        // Capture the user saved to DB to check the role
        verify(userRepository).save(argThat(user -> user.getRole().equals("ADMIN")));
    }

    @Test
    @DisplayName("ROLE: Should assign CUSTOMER role for normal names")
    void registerUser_AssignCustomerRole() {
        // Arrange
        when(userRepository.findByUsername("customer_alice")).thenReturn(null);
        when(passwordEncoder.encode("pass123")).thenReturn("hashed_pass");

        // Act
        userService.registerUser("customer_alice", "alice@mail.com", "pass123", "Street 1");

        // Assert
        verify(userRepository).save(argThat(user -> user.getRole().equals("CUSTOMER")));
    }

    @Test
    @DisplayName("VALIDATION: Should return error message if user exists")
    void registerUser_UserExists() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(mockUser);

        // Act
        String result = userService.registerUser("testuser", "new@mail.com", "pass", "addr");

        // Assert
        assertEquals("User already exists!", result);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("PASSWORD: validateUser should return true for correct password")
    void validateUser_Success() {
        // Arrange
        mockUser.setPassword("hashed_pass");
        when(userRepository.findByUsername("testuser")).thenReturn(mockUser);
        when(passwordEncoder.matches("plain_pass", "hashed_pass")).thenReturn(true);

        // Act
        boolean isValid = userService.validateUser("testuser", "plain_pass");

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("UPDATE: Should change username and address")
    void updateUserDetails_Success() {
        // Arrange
        when(userRepository.findByUsername("oldUser")).thenReturn(mockUser);

        // Act
        userService.updateUserDetails("oldUser", "newUser", "New Address");

        // Assert
        assertEquals("newUser", mockUser.getUsername());
        assertEquals("New Address", mockUser.getAddress());
        verify(userRepository).save(mockUser);
    }
}
