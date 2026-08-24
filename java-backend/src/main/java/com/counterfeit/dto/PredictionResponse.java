package com.counterfeit.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) for sending prediction results back to the client.
 * This class gathers all the important information about a single prediction.
 */
public class PredictionResponse {

    private Long id;                     // The unique ID of the prediction record
    private String userName;             // The name of the user who made the prediction
    private String imageName;            // The original name of the uploaded image file
    private String imagePath;            // The path or URL where the image is stored
    private String productCategory;      // The category of the product (e.g., Shoes, Watches)
    private String prediction;           // The result of the model (e.g., "Genuine", "Counterfeit")
    private BigDecimal confidence;       // The confidence score of the model's prediction (e.g., 98.5)
    private String modelUsed;            // The name or version of the machine learning model used
    private LocalDateTime predictedAt;   // The date and time when the prediction was made

    // Getters and Setters for all fields

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public String getPrediction() {
        return prediction;
    }

    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }

    public String getModelUsed() {
        return modelUsed;
    }

    public void setModelUsed(String modelUsed) {
        this.modelUsed = modelUsed;
    }

    public LocalDateTime getPredictedAt() {
        return predictedAt;
    }

    public void setPredictedAt(LocalDateTime predictedAt) {
        this.predictedAt = predictedAt;
    }
}
