package com.counterfeit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a record of a counterfeit prediction made by a user.
 * Mapped to the "prediction_history" table.
 */
@Entity
@Table(name = "prediction_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredictionHistory {

    @Id // Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incrementing
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Many predictions can belong to one user. LAZY means user data is loaded only when requested.
    @JoinColumn(name = "user_id", nullable = true) // Creates a foreign key column named "user_id" (nullable for guest uploads)
    private User user;

    @Column(name = "image_name", length = 255) // Maps to "image_name" in the DB
    private String imageName;

    @Column(name = "image_path", length = 500) // Stores the path where the image is saved
    private String imagePath;

    @Column(name = "product_category", length = 50)
    private String productCategory = "other"; // Default value

    @Column(nullable = false, length = 20)
    private String prediction; // e.g., "Genuine" or "Counterfeit"

    @Column(precision = 5, scale = 2) // Supports numbers like 100.00 (5 total digits, 2 decimal places)
    private BigDecimal confidence;

    @Column(name = "model_used", length = 100)
    private String modelUsed = "MobileNetV2"; // Default value

    @Column(name = "predicted_at", updatable = false) // The timestamp when the prediction was made
    private LocalDateTime predictedAt;

    /**
     * Runs before the entity is inserted into the database.
     * Automatically sets the current timestamp.
     */
    @PrePersist
    protected void onCreate() {
        predictedAt = LocalDateTime.now();
    }
}
