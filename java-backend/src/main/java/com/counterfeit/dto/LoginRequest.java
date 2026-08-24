package com.counterfeit.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object (DTO) for Login requests.
 * This class holds the information sent by the user when they try to log in.
 */
public class LoginRequest {

    // Ensures that the email field is not empty and has a valid email format.
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

    // Ensures that the password field is not left empty.
    @NotBlank(message = "Password cannot be empty")
    private String password;

    // Getter for email
    public String getEmail() {
        return email;
    }

    // Setter for email
    public void setEmail(String email) {
        this.email = email;
    }

    // Getter for password
    public String getPassword() {
        return password;
    }

    // Setter for password
    public void setPassword(String password) {
        this.password = password;
    }
}
