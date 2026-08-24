package com.counterfeit.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a User in the system.
 * This class is mapped to the "users" table in the database.
 */
@Entity // Tells Hibernate to make a table out of this class
@Table(name = "users") // Specifies the exact name of the database table
@Data // Lombok annotation to automatically generate getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Lombok annotation to generate a no-arguments constructor
@AllArgsConstructor // Lombok annotation to generate an all-arguments constructor
public class User {

    @Id // Marks this field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Automatically generates an auto-incrementing ID
    private Long id;

    @Column(nullable = false, length = 100) // Column cannot be null, max length is 100
    private String name;

    @Column(nullable = false, length = 150, unique = true) // Column cannot be null, must be unique (no duplicate emails)
    private String email;

    @JsonIgnore // Prevents the password from being serialized and sent in JSON API responses
    @Column(nullable = false, length = 255) // Column cannot be null, max length is 255
    private String password;

    @Column(nullable = false, length = 10) // Role string, e.g., 'USER' or 'ADMIN'
    private String role = "USER";

    @Column(name = "created_at", updatable = false) // Maps to 'created_at', and tells Hibernate this should never be updated after creation
    private LocalDateTime createdAt;
    
    /**
     * A lifecycle callback that runs before the entity is saved for the first time.
     * Sets the creation timestamp automatically.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
