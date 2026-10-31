package com.counterfeit.controller;

import com.counterfeit.dto.DashboardStats;
import com.counterfeit.dto.PredictionResponse;
import com.counterfeit.service.PredictionAnalysisUnavailableException;
import com.counterfeit.service.PredictionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * PredictionController exposes endpoints for analyzing product images, retrieving history, and admin controls.
 */
@RestController
@RequestMapping("/api/predictions")
public class PredictionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PredictionController.class);
    private static final int MAX_IMAGE_WIDTH = 4096;
    private static final int MAX_IMAGE_HEIGHT = 4096;
    private static final long MAX_IMAGE_PIXELS = 12_000_000L;

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    /**
     * Uploads a product image, validates format/size/dimensions, sends it to the AI microservice, and saves the result.
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

        // Validate compressed upload size before reading image content.
        if (file.getSize() > 10 * 1024 * 1024) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "File size exceeds 10MB maximum limit.");
            return ResponseEntity.badRequest().body(error);
        }

        // Validate Image MIME Content Type or File Extension.
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

        // Inspect dimensions from image metadata before allocating the decoded pixel buffer.
        try (InputStream inputStream = file.getInputStream();
             ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {

            if (imageInputStream == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Corrupted or invalid image file structure.");
                return ResponseEntity.badRequest().body(error);
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
            if (!readers.hasNext()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unsupported or invalid image file.");
                return ResponseEntity.badRequest().body(error);
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInputStream, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                long pixelCount = (long) width * height;

                if (width <= 0 || height <= 0
                        || width > MAX_IMAGE_WIDTH
                        || height > MAX_IMAGE_HEIGHT
                        || pixelCount > MAX_IMAGE_PIXELS) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Image dimensions exceed the allowed limit of 4096 x 4096 pixels and 12 megapixels.");
                    return ResponseEntity.badRequest().body(error);
                }

                BufferedImage bufferedImage = reader.read(0);
                if (bufferedImage == null) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Corrupted or invalid image file structure.");
                    return ResponseEntity.badRequest().body(error);
                }
            } finally {
                reader.dispose();
            }
        } catch (Exception e) {
            LOGGER.warn("Unable to inspect uploaded product image", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unable to decode image file. Please upload a valid image.");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            PredictionResponse result = predictionService.analyzeAndSave(file, userId, category);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (PredictionAnalysisUnavailableException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        } catch (Exception e) {
            LOGGER.error("Failed to analyze uploaded product image", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to analyze image. Please try again later.");
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

        // IDOR Prevention: If requester is a normal USER, they cannot view another user's history.
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
