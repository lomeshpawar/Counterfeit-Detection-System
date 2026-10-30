package com.counterfeit.controller;

import com.counterfeit.service.PredictionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PredictionControllerTest {

    @Mock
    private PredictionService predictionService;

    @InjectMocks
    private PredictionController predictionController;

    @Test
    void analyzeDoesNotExposeInternalExceptionDetails() throws Exception {
        MockMultipartFile file = pngImage("product.png", 1, 1);

        when(predictionService.analyzeAndSave(file, 42L, "footwear"))
                .thenThrow(new IOException("internal database detail"));

        ResponseEntity<?> response = predictionController.analyzeProductImage(file, 42L, "footwear");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Failed to analyze image. Please try again later.", body.get("error"));
        assertFalse(body.get("error").toString().contains("internal database detail"));
    }

    @Test
    void rejectsImagesAboveMaximumWidthBeforeCallingPredictionService() throws Exception {
        MockMultipartFile file = pngImage("wide.png", 4097, 1);

        ResponseEntity<?> response = predictionController.analyzeProductImage(file, 42L, "footwear");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());
        assertTrue(((Map<?, ?>) response.getBody()).get("error").toString().contains("dimensions"));
        verifyNoInteractions(predictionService);
    }

    @Test
    void rejectsImagesAbovePixelLimitBeforeCallingPredictionService() throws Exception {
        MockMultipartFile file = pngImage("large.png", 3500, 3500);

        ResponseEntity<?> response = predictionController.analyzeProductImage(file, 42L, "footwear");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());
        assertTrue(((Map<?, ?>) response.getBody()).get("error").toString().contains("12 megapixels"));
        verifyNoInteractions(predictionService);
    }

    private MockMultipartFile pngImage(String filename, int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
        ImageIO.write(image, "png", imageBytes);
        return new MockMultipartFile("file", filename, "image/png", imageBytes.toByteArray());
    }
}
