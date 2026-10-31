package com.counterfeit.service;

import com.counterfeit.dto.DashboardStats;
import com.counterfeit.dto.PredictionResponse;
import com.counterfeit.entity.PredictionHistory;
import com.counterfeit.entity.User;
import com.counterfeit.repository.PredictionHistoryRepository;
import com.counterfeit.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Coordinates image analysis through the Flask service and persists validated results.
 */
@Service
public class PredictionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PredictionService.class);
    private static final Set<String> ALLOWED_PREDICTIONS = Set.of("Genuine", "Counterfeit");
    private static final BigDecimal MIN_CONFIDENCE = BigDecimal.ZERO;
    private static final BigDecimal MAX_CONFIDENCE = new BigDecimal("100.00");

    private final PredictionHistoryRepository predictionRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${app.upload.dir:./uploads/}")
    private String uploadDir;

    @Value("${ai.service.url:http://localhost:5000/predict}")
    private String aiServiceUrl;

    public PredictionService(PredictionHistoryRepository predictionRepository,
                             UserRepository userRepository,
                             RestTemplate restTemplate) {
        this.predictionRepository = predictionRepository;
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
    }

    /**
     * Saves an image and history record only after the AI service returns a valid result.
     */
    public PredictionResponse analyzeAndSave(MultipartFile file, Long userId, String category) throws IOException {
        byte[] imageBytes = file.getBytes();
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "product-image";
        }

        PredictionResult predictionResult = requestPrediction(imageBytes, originalFilename);

        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }

        Path uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadDirectory);

        String uniqueFilename = UUID.randomUUID() + safeExtension(originalFilename);
        Path savedFile = uploadDirectory.resolve(uniqueFilename).normalize();
        if (!savedFile.startsWith(uploadDirectory)) {
            throw new IOException("Invalid upload path.");
        }

        Files.write(savedFile, imageBytes, StandardOpenOption.CREATE_NEW);
        String webImagePath = "/uploads/" + uniqueFilename;

        PredictionHistory history = new PredictionHistory();
        history.setUser(user);
        history.setImageName(originalFilename);
        history.setImagePath(webImagePath);
        history.setProductCategory(category != null ? category : "other");
        history.setPrediction(predictionResult.prediction());
        history.setConfidence(predictionResult.confidence());
        history.setModelUsed(predictionResult.modelUsed());
        history.setPredictedAt(LocalDateTime.now());

        PredictionHistory saved;
        try {
            saved = predictionRepository.save(history);
        } catch (RuntimeException persistenceFailure) {
            try {
                Files.deleteIfExists(savedFile);
            } catch (IOException cleanupFailure) {
                LOGGER.warn("Unable to remove uploaded image after persistence failure", cleanupFailure);
            }
            throw persistenceFailure;
        }
        return convertToResponse(saved);
    }

    private PredictionResult requestPrediction(byte[] imageBytes, String originalFilename) {
        try {
            ByteArrayResource fileResource = new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return originalFilename;
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", fileResource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, new HttpHeaders());
            ResponseEntity<Map> response = restTemplate.postForEntity(aiServiceUrl, requestEntity, Map.class);

            if (response == null || !response.getStatusCode().is2xxSuccessful()
                    || response.getBody() == null) {
                throw new IllegalStateException("AI service returned no successful prediction response.");
            }

            Map<String, Object> responseBody = response.getBody();
            Object predictionValue = responseBody.get("prediction");
            if (!(predictionValue instanceof String prediction)
                    || !ALLOWED_PREDICTIONS.contains(prediction)) {
                throw new IllegalStateException("AI service returned an unsupported prediction label.");
            }

            Object confidenceValue = responseBody.get("confidence");
            if (confidenceValue == null) {
                throw new IllegalStateException("AI service response is missing confidence.");
            }

            BigDecimal confidence = new BigDecimal(confidenceValue.toString())
                    .setScale(2, RoundingMode.HALF_UP);
            if (confidence.compareTo(MIN_CONFIDENCE) < 0
                    || confidence.compareTo(MAX_CONFIDENCE) > 0) {
                throw new IllegalStateException("AI service returned an out-of-range confidence value.");
            }

            Object modelValue = responseBody.get("model_used");
            if (!(modelValue instanceof String modelUsed)
                    || modelUsed.isBlank()
                    || modelUsed.length() > 100) {
                throw new IllegalStateException("AI service response contains an invalid model identifier.");
            }

            return new PredictionResult(prediction, confidence, modelUsed);
        } catch (Exception analysisFailure) {
            LOGGER.error("AI service call failed or returned an invalid prediction response", analysisFailure);
            throw new PredictionAnalysisUnavailableException();
        }
    }

    private String safeExtension(String filename) {
        int extensionStart = filename.lastIndexOf('.');
        if (extensionStart < 0) {
            return ".img";
        }

        String extension = filename.substring(extensionStart).toLowerCase(Locale.ROOT);
        return extension.matches("\\.[a-z0-9]{1,8}") ? extension : ".img";
    }

    public List<PredictionResponse> getUserHistory(Long userId) {
        return predictionRepository.findByUserIdOrderByPredictedAtDesc(userId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<PredictionResponse> getAllPredictions() {
        return predictionRepository.findAllByOrderByPredictedAtDesc()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public void deletePrediction(Long id) {
        predictionRepository.deleteById(id);
    }

    public DashboardStats getDashboardStats() {
        long totalUsers = userRepository.countByRole("USER");
        long totalPredictions = predictionRepository.count();
        long genuineCount = predictionRepository.countByPrediction("Genuine");
        long counterfeitCount = predictionRepository.countByPrediction("Counterfeit");

        return new DashboardStats(totalUsers, totalPredictions, genuineCount, counterfeitCount);
    }

    private PredictionResponse convertToResponse(PredictionHistory history) {
        PredictionResponse response = new PredictionResponse();
        response.setId(history.getId());

        if (history.getUser() != null) {
            response.setUserName(history.getUser().getName());
        } else {
            response.setUserName("Guest User");
        }

        response.setImageName(history.getImageName());
        response.setImagePath(history.getImagePath());
        response.setProductCategory(history.getProductCategory());
        response.setPrediction(history.getPrediction());
        response.setConfidence(history.getConfidence());
        response.setModelUsed(history.getModelUsed());
        response.setPredictedAt(history.getPredictedAt());

        return response;
    }

    private record PredictionResult(String prediction, BigDecimal confidence, String modelUsed) {
    }
}
