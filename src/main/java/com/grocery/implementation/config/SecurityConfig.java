package com.grocery.implementation.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.grocery.implementation.service.CustomUserDetailsService; // 🔑 Changed import

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService; // 🔑 Use the Service, not UserDetails
    private final CustomSuccessHandler customSuccessHandler;
    @Autowired
    public SecurityConfig(@Lazy CustomUserDetailsService customUserDetailsService, CustomSuccessHandler customSuccessHandler) {
        this.customUserDetailsService = customUserDetailsService;
        this.customSuccessHandler = customSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(request -> request
                        // Public pages
                        .requestMatchers("/", "/products", "/products/**","/register", "/login", "/about", "/contact","/api/register","/images/**", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/agent/**").hasRole("DELIVERY_AGENT")
                        // Admin-only pages - hasRole("ADMIN") looks for "ROLE_ADMIN" internally
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Customer/Authenticated pages
                        .requestMatchers("/api/products/**", "/cart/**", "/orders/**").authenticated()
                        .anyRequest().authenticated())

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(customSuccessHandler)
                        .permitAll())


                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true) // Destroys session
                        .clearAuthentication(true)   // Clears security context
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .userDetailsService(customUserDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}