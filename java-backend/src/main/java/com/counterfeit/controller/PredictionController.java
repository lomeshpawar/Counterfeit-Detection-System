package com.counterfeit.controller;

import com.counterfeit.dto.DashboardStats;
import com.counterfeit.dto.PredictionResponse;
import com.counterfeit.service.PredictionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PredictionController exposes endpoints for analyzing product images, retrieving history, and admin controls.
 */
@RestController
@RequestMapping("/api/predictions")
@CrossOrigin(origins = "*")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    /**
     * Uploads a product image, validates format/size, sends to AI microservice, and saves result.
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeProductImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "category", defaultValue = "other") String category) {
        
        if (file == null || file.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Please upload a valid image file.");
            return ResponseEntity.badRequest().body(error);
        }

        // Validate File Size (Max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "File size exceeds 10MB maximum limit.");
            return ResponseEntity.badRequest().body(error);
        }

        // Validate Image MIME Content Type or File Extension
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        String lowerFilename = originalFilename != null ? originalFilename.toLowerCase() : "";

        boolean isImageMime = contentType != null && contentType.toLowerCase().startsWith("image/");
        boolean isImageExt = lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg") 
            || lowerFilename.endsWith(".png") || lowerFilename.endsWith(".webp") || lowerFilename.endsWith(".bmp");

        if (!isImageMime && !isImageExt) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid file format. Only JPG, JPEG, PNG, and WebP images are permitted.");
            return ResponseEntity.badRequest().body(error);
        }

        // Deep Content Inspection: Verify file is actual image data
        try {
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if (bufferedImage == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Corrupted or invalid image file structure.");
                return ResponseEntity.badRequest().body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unable to decode image file content: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }

        try {
            PredictionResponse result = predictionService.analyzeAndSave(file, userId, category);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to analyze image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Retrieves prediction history for a specific user (IDOR protected).
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getUserHistory(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long reqUserId,
            @RequestHeader(value = "X-User-Role", required = false) String reqUserRole,
            @RequestHeader(value = "X-Admin-Role", required = false) String reqAdminRole) {
        
        boolean isAdmin = "ADMIN".equalsIgnoreCase(reqUserRole) || "ADMIN".equalsIgnoreCase(reqAdminRole);

        // IDOR Prevention: If requester is a normal USER, they cannot view another user's history
        if (!isAdmin && reqUserId != null && !reqUserId.equals(userId)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Access Denied: You are not authorized to view another user's private prediction records.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        List<PredictionResponse> history = predictionService.getUserHistory(userId);
        return ResponseEntity.ok(history);
    }

    /**
     * Retrieves all predictions in the system (for admin dashboard only).
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllPredictions(
            @RequestHeader(value = "X-Admin-Role", required = false) String adminRole,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        boolean isAdmin = "ADMIN".equalsIgnoreCase(adminRole) || "ADMIN".equalsIgnoreCase(userRole);
        if (!isAdmin) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Access Denied: Admin authorization required.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
        List<PredictionResponse> allPredictions = predictionService.getAllPredictions();
        return ResponseEntity.ok(allPredictions);
    }

    /**
     * Deletes a specific prediction by ID (Admin only).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePrediction(
            @PathVariable Long id,
            @RequestHeader(value = "X-Admin-Role", required = false) String adminRole,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        boolean isAdmin = "ADMIN".equalsIgnoreCase(adminRole) || "ADMIN".equalsIgnoreCase(userRole);
        if (!isAdmin) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Access Denied: Admin authorization required.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
        predictionService.deletePrediction(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Prediction deleted successfully.");
        return ResponseEntity.ok(response);
    }

    /**
     * Gets statistics for the admin dashboard (Admin only).
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats(
            @RequestHeader(value = "X-Admin-Role", required = false) String adminRole,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        boolean isAdmin = "ADMIN".equalsIgnoreCase(adminRole) || "ADMIN".equalsIgnoreCase(userRole);
        if (!isAdmin) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Access Denied: Admin authorization required.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
        DashboardStats stats = predictionService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }
}
