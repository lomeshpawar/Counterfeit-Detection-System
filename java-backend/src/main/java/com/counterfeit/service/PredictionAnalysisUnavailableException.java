package com.counterfeit.service;

/**
 * Indicates that a trustworthy prediction could not be obtained from the AI service.
 */
public class PredictionAnalysisUnavailableException extends RuntimeException {

    public PredictionAnalysisUnavailableException() {
        super("AI analysis is temporarily unavailable. Please try again later.");
    }
}
