package com.grocery.implementation.controller;

import com.grocery.implementation.model.UserModel;
import com.grocery.implementation.repository.UserRepository;
import com.grocery.implementation.service.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     * Displays the user profile page.
     */
    @GetMapping("/profile")
    public String viewProfile(Model model, Authentication authentication) {
        String username = authentication.getName();
        UserModel user = userRepository.findByUsername(username);
        model.addAttribute("user", user);
        return "profile"; // Refers to profile.html
    }

    /**
     * Handles the profile update form submission.
     */
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam("username") String newUsername,
                                @RequestParam("address") String newAddress,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            String currentUsername = authentication.getName();

            // 1. Update database via service
            UserDetails updatedUser = userService.updateUserDetails(currentUsername, newUsername, newAddress);

            // 2. Refresh Security Session with new credentials
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    updatedUser,
                    authentication.getCredentials(),
                    updatedUser.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Update failed: " + e.getMessage());
        }
        return "redirect:/profile";
    }
}