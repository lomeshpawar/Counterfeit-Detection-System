package com.counterfeit.service;

import com.counterfeit.dto.PredictionResponse;
import com.counterfeit.entity.PredictionHistory;
import com.counterfeit.repository.PredictionHistoryRepository;
import com.counterfeit.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PredictionServiceTest {

    private static final String AI_SERVICE_URL = "http://localhost:5000/predict";

    @Mock
    private PredictionHistoryRepository predictionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PredictionService predictionService;

    @TempDir
    Path tempDirectory;

    private Path uploadDirectory;

    @BeforeEach
    void configureService() {
        uploadDirectory = tempDirectory.resolve("uploads");
        ReflectionTestUtils.setField(predictionService, "uploadDir", uploadDirectory.toString());
        ReflectionTestUtils.setField(predictionService, "aiServiceUrl", AI_SERVICE_URL);
    }

    @Test
    void persistsOnlyAValidatedPredictionAndItsImage() throws Exception {
        MockMultipartFile image = image("shoe.png");
        Map<String, Object> aiResult = Map.of(
                "prediction", "Genuine",
                "confidence", 92.5,
                "model_used", "Computer Vision Texture Inspector"
        );
        whenAiReturns(aiResult);
        when(predictionRepository.save(any(PredictionHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PredictionResponse result = predictionService.analyzeAndSave(image, null, "footwear");

        assertEquals("Genuine", result.getPrediction());
        assertEquals(new BigDecimal("92.50"), result.getConfidence());
        assertEquals("Computer Vision Texture Inspector", result.getModelUsed());
        verify(predictionRepository).save(any(PredictionHistory.class));
        try (Stream<Path> files = Files.list(uploadDirectory)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    void storesImageUsingValidatedContentFormatInsteadOfClientFilenameExtension() throws Exception {
        MockMultipartFile image = image("payload.html");
        whenAiReturns(Map.of(
                "prediction", "Genuine",
                "confidence", 92.5,
                "model_used", "Computer Vision Texture Inspector"
        ));
        when(predictionRepository.save(any(PredictionHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PredictionResponse result = predictionService.analyzeAndSave(image, null, "footwear");

        assertTrue(result.getImagePath().endsWith(".png"));
        try (Stream<Path> files = Files.list(uploadDirectory)) {
            Path storedImage = files.findFirst().orElseThrow();
            assertTrue(storedImage.getFileName().toString().endsWith(".png"));
            assertArrayEquals(image.getBytes(), Files.readAllBytes(storedImage));
        }
    }

    @Test
    void rejectsUnsupportedImageContentBeforeCallingAiService() {
        MockMultipartFile invalidImage = new MockMultipartFile(
                "file", "payload.png", "image/png", new byte[]{1, 2, 3, 4}
        );

        assertThrows(
                IOException.class,
                () -> predictionService.analyzeAndSave(invalidImage, null, "footwear")
        );

        verifyNoInteractions(restTemplate, predictionRepository, userRepository);
        assertFalse(Files.exists(uploadDirectory));
    }

    @Test
    void doesNotPersistOrStoreImageWhenAiServiceConnectionFails() {
        when(restTemplate.postForEntity(
                eq(AI_SERVICE_URL), any(HttpEntity.class), eq(Map.class)
        )).thenThrow(new ResourceAccessException("connection refused"));

        PredictionAnalysisUnavailableException exception = assertThrows(
                PredictionAnalysisUnavailableException.class,
                () -> predictionService.analyzeAndSave(image("shoe.png"), null, "footwear")
        );

        assertEquals("AI analysis is temporarily unavailable. Please try again later.", exception.getMessage());
        verifyNoInteractions(predictionRepository, userRepository);
        assertFalse(Files.exists(uploadDirectory));
    }

    @Test
    void doesNotPersistWhenAiServiceReturnsNon2xxResponse() {
        when(restTemplate.postForEntity(
                eq(AI_SERVICE_URL), any(HttpEntity.class), eq(Map.class)
        )).thenReturn(new ResponseEntity<Map>(null, HttpStatus.SERVICE_UNAVAILABLE));

        assertThrows(
                PredictionAnalysisUnavailableException.class,
                () -> predictionService.analyzeAndSave(image("shoe.png"), null, "footwear")
        );

        verifyNoInteractions(predictionRepository, userRepository);
        assertFalse(Files.exists(uploadDirectory));
    }

    @Test
    void rejectsIncompleteAiResponseWithoutPersisting() {
        whenAiReturns(Map.of("prediction", "Genuine", "confidence", 92.5));

        assertThrows(
                PredictionAnalysisUnavailableException.class,
                () -> predictionService.analyzeAndSave(image("shoe.png"), null, "footwear")
        );

        verifyNoInteractions(predictionRepository, userRepository);
        assertFalse(Files.exists(uploadDirectory));
    }

    @Test
    void rejectsUnsupportedPredictionLabelWithoutPersisting() {
        whenAiReturns(Map.of(
                "prediction", "Unknown",
                "confidence", 92.5,
                "model_used", "Experimental heuristic"
        ));

        assertThrows(
                PredictionAnalysisUnavailableException.class,
                () -> predictionService.analyzeAndSave(image("shoe.png"), null, "footwear")
        );

        verifyNoInteractions(predictionRepository, userRepository);
        assertFalse(Files.exists(uploadDirectory));
    }

    @Test
    void rejectsOutOfRangeConfidenceWithoutPersisting() {
        whenAiReturns(Map.of(
                "prediction", "Genuine",
                "confidence", 140.0,
                "model_used", "Experimental heuristic"
        ));

        assertThrows(
                PredictionAnalysisUnavailableException.class,
                () -> predictionService.analyzeAndSave(image("shoe.png"), null, "footwear")
        );

        verifyNoInteractions(predictionRepository, userRepository);
        assertFalse(Files.exists(uploadDirectory));
    }

    @Test
    void removesStoredImageWhenPredictionHistoryPersistenceFails() throws Exception {
        whenAiReturns(Map.of(
                "prediction", "Genuine",
                "confidence", 92.5,
                "model_used", "Computer Vision Texture Inspector"
        ));
        when(predictionRepository.save(any(PredictionHistory.class)))
                .thenThrow(new IllegalStateException("database unavailable"));

        assertThrows(
                IllegalStateException.class,
                () -> predictionService.analyzeAndSave(image("shoe.png"), null, "footwear")
        );

        verify(predictionRepository).save(any(PredictionHistory.class));
        try (Stream<Path> files = Files.list(uploadDirectory)) {
            assertEquals(0, files.count());
        }
    }

    private void whenAiReturns(Map<String, Object> response) {
        ResponseEntity<Map> aiResponse = new ResponseEntity<Map>(response, HttpStatus.OK);
        when(restTemplate.postForEntity(
                eq(AI_SERVICE_URL), any(HttpEntity.class), eq(Map.class)
        )).thenReturn(aiResponse);
    }

    private MockMultipartFile image(String filename) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            BufferedImage source = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
            if (!ImageIO.write(source, "png", output)) {
                throw new IllegalStateException("PNG writer is unavailable.");
            }
            return new MockMultipartFile("file", filename, "image/png", output.toByteArray());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create test image.", exception);
        }
    }
}
