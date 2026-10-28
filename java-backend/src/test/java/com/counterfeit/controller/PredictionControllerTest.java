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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PredictionControllerTest {

    @Mock
    private PredictionService predictionService;

    @InjectMocks
    private PredictionController predictionController;

    @Test
    void analyzeDoesNotExposeInternalExceptionDetails() throws Exception {
        ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB), "png", imageBytes);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "product.png",
                "image/png",
                imageBytes.toByteArray()
        );

        when(predictionService.analyzeAndSave(file, 42L, "footwear"))
                .thenThrow(new IOException("internal database detail"));

        ResponseEntity<?> response = predictionController.analyzeProductImage(file, 42L, "footwear");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Failed to analyze image. Please try again later.", body.get("error"));
        assertFalse(body.get("error").toString().contains("internal database detail"));
    }
}
