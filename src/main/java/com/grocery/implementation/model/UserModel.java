package com.grocery.implementation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size; // 🎯 This will now stop being red
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

@Entity
@Table(name = "user")
public class UserModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
	private Integer id;
	private String username;
    @Column(nullable = false)
    @jakarta.validation.constraints.Size(min = 6, message = "Password must be at least 6 characters")
	private String password;
    @Column(name = "address", length = 500)
    private String address;
	private String role;
    private  String email;
    @Column(name = "walletBalance")
    private BigDecimal walletBalance = BigDecimal.ZERO;

    // Getters and Setters
    public BigDecimal getWalletBalance() { return walletBalance; }
    public void setWalletBalance(BigDecimal walletBalance) { this.walletBalance = walletBalance; }
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

}
