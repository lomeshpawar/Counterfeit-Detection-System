package com.counterfeit.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) for User Registration requests.
 * DTOs are simple classes used to transfer data between the client (like a web browser or mobile app) 
 * and the server.
 */
public class RegisterRequest {

    // @NotBlank ensures that the user provides a name and it's not just spaces.
    @NotBlank(message = "Name cannot be empty")
    private String name;

    // @NotBlank ensures it's not empty, and @Email checks if it's a valid email format.
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

    // @NotBlank ensures it's not empty, and @Size ensures the password is at least 6 characters long.
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    // Getters and Setters are used to safely access and update the private fields.
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
