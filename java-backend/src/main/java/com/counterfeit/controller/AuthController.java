package com.counterfeit.controller;

import com.counterfeit.dto.LoginRequest;
import com.counterfeit.dto.LoginResponse;
import com.counterfeit.dto.RegisterRequest;
import com.counterfeit.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AuthController manages user authentication endpoints.
 * 
 * @RestController tells Spring that this class will handle HTTP web requests and return JSON responses.
 * @RequestMapping defines the base URL for all endpoints in this class.
 * @CrossOrigin allows web applications running on different ports (like React/Vue) to communicate with this backend.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Allow requests from any origin (e.g., local frontend development)
public class AuthController {

    private final UserService userService;

    // Injecting the UserService
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint to register a new user.
     * @PostMapping maps HTTP POST requests to this method.
     * @RequestBody tells Spring to convert the incoming JSON request into a RegisterRequest object.
     * @Valid ensures the data meets our constraints (e.g., email format, password length).
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // Call the service to perform the business logic
            userService.registerUser(request);
            
            // Return a success message with HTTP status 201 (Created)
            Map<String, String> response = new HashMap<>();
            response.put("message", "Registration successful!");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            // If something goes wrong (e.g., email already exists), return a 400 (Bad Request)
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Endpoint for user login.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.loginUser(request);
            return ResponseEntity.ok(response); // HTTP 200 OK
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error); // HTTP 401 Unauthorized
        }
    }

    /**
     * Endpoint for admin login.
     */
    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.loginAdmin(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
}
