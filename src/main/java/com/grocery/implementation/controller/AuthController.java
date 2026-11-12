package com.grocery.implementation.controller;
import com.grocery.implementation.model.UserModel;
import com.grocery.implementation.repository.UserRepository;
import com.grocery.implementation.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.grocery.implementation.service.UserService;

@Controller
public class AuthController {
    @Autowired
    private PasswordEncoder passwordEncoder;
	@Autowired
	private UserService userService;
    private ProductService productService;
    @Autowired
    private UserRepository userRepository; // 🎯 Added this
    @GetMapping("/")
    public String showLandingPage(Authentication authentication, Model model) {
        model.addAttribute("isAuthenticated", authentication != null && authentication.isAuthenticated());
        return "index"; // 🎯 This loads templates/index.html instead of redirecting
    }
	@GetMapping("/register")
	public String register() {
		return "register";
	}
	@PostMapping("/register")
	public String registerUser(@RequestParam String username,@RequestParam String email,@RequestParam String password,@RequestParam String address,Model model) {
        if (password == null || password.length() < 6) {
            model.addAttribute("passError", "Password must be at least 6 characters!");
            // Keep entered values so user doesn't have to re-type
            model.addAttribute("enteredUsername", username);
            model.addAttribute("enteredEmail", email);
            model.addAttribute("enteredAddress", address);
            return "register";
        }
        String result = userService.registerUser(username, email, password,address);
        if (!result.equals("success")) {
            model.addAttribute("error", result);
            model.addAttribute("enteredUsername", username);
            model.addAttribute("enteredEmail", email);
            model.addAttribute("enteredAddress", address);
            return "register";
        }
        return "redirect:/login";
        //userService.registerUser(username, email,password);
		//return "redirect:/login";
	}
    @PostMapping("/api/register")
    @ResponseBody
    public ResponseEntity<?> registerUserApi(@RequestBody UserModel user) {
        // Check if user exists first (optional but good practice)
        if (userService.isUsernameValid(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("exists");
        }
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password must be at least 6 characters.");
        }
        // Use a modified version of your registration logic that encodes passwords
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) user.setRole("CUSTOMER");

        userService.createRest(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
	@GetMapping("/login")
	public String login() {
		return "login";
	}



    @GetMapping("/welcome")
	public String welcome() {
		return "welcome";
	}
//    @GetMapping("/profile")
//    public String viewProfile(Model model, Authentication authentication) {
//        String username = authentication.getName();
//        UserModel user = userRepository.findByUsername(username);
//
//        model.addAttribute("user", user);
//        return "profile";
//    }
    @GetMapping("/about")
    public String aboutPage() {
        return "about";
    }

    @GetMapping("/contact")
    public String contactPage() {
        return "contact";
    }
}
