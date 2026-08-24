package com.counterfeit.service;

import com.counterfeit.dto.LoginRequest;
import com.counterfeit.dto.LoginResponse;
import com.counterfeit.dto.RegisterRequest;
import com.counterfeit.entity.User;
import com.counterfeit.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * The UserService handles the core business logic related to users, such as registration and login.
 * 
 * @Service annotation tells Spring that this class is a Service component. 
 * Spring will automatically create an instance of this class and manage it (Dependency Injection).
 */
@Service
public class UserService {

    // Repositories are used to interact with the database.
    private final UserRepository userRepository;
    
    // PasswordEncoder is used to safely hash passwords before saving them, and to verify passwords on login.
    private final PasswordEncoder passwordEncoder;

    // Constructor-based Dependency Injection. Spring automatically provides the required objects when creating this service.
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user.
     * @param request the registration details provided by the user.
     * @return the saved User entity.
     */
    public User registerUser(RegisterRequest request) {
        // 1. Check if a user with the given email already exists in the database.
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            // If the user exists, throw an exception to stop registration.
            throw new RuntimeException("Email already registered!");
        }

        // 2. Create a new User entity and populate it with data from the request.
        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        
        // Hash the password for security. Never store plain text passwords!
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // Set the default role for a new user to "USER".
        newUser.setRole("USER");

        // 3. Save the new user to the database and return it.
        return userRepository.save(newUser);
    }

    /**
     * Authenticates a user based on email and password.
     * @param request the login details.
     * @return a LoginResponse with user details.
     */
    public LoginResponse loginUser(LoginRequest request) {
        // 1. Try to find the user by their email.
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password.")); // Throw error if not found

        // 2. Check if the provided password matches the hashed password in the database.
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password.");
        }

        // 3. If login is successful, create and return a LoginResponse.
        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                "Login successful."
        );
    }

    /**
     * Authenticates an admin user. Similar to normal login but enforces the "ADMIN" role.
     * @param request the admin login details.
     * @return a LoginResponse with admin details.
     */
    public LoginResponse loginAdmin(LoginRequest request) {
        // 1. Try to find the admin by their email.
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password."));

        // 2. Verify password.
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password.");
        }

        // 3. Verify that the user has the ADMIN role.
        if (!"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("Access denied. Not an admin account.");
        }

        // 4. Return the response if successful.
        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                "Login successful."
        );
    }

    /**
     * Gets the total count of users with the role "USER".
     * @return count of regular users.
     */
    public long getUserCount() {
        return userRepository.countByRole("USER");
    }
}
