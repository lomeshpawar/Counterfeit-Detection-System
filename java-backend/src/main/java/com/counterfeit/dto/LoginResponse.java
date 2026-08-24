package com.counterfeit.dto;

/**
 * Data Transfer Object (DTO) for Login responses.
 * When a user successfully logs in, the server will send back this object containing
 * their details and a success message.
 */
public class LoginResponse {

    private Long id;       // The unique ID of the user in the database
    private String name;   // The name of the user
    private String email;  // The email of the user
    private String role;   // The role of the user (e.g., "USER" or "ADMIN")
    private String message; // A message indicating the result of the login attempt (e.g., "Login successful")

    // Default constructor
    public LoginResponse() {
    }

    // Constructor to easily create a response object with all fields
    public LoginResponse(Long id, String name, String email, String role, String message) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.message = message;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
