package com.counterfeit.repository;

import com.counterfeit.entity.PredictionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for PredictionHistory entity.
 * Handles database operations for prediction records.
 */
@Repository
public interface PredictionHistoryRepository extends JpaRepository<PredictionHistory, Long> {

    /**
     * Finds all prediction history records for a specific user, ordered by the prediction time (newest first).
     * Great for a user's personal history page.
     * @param userId The ID of the user.
     * @return A list of PredictionHistory records.
     */
    List<PredictionHistory> findByUserIdOrderByPredictedAtDesc(Long userId);

    /**
     * Finds all predictions from all users, ordered by prediction time (newest first).
     * Useful for an admin view to see recent activity.
     * @return A list of all PredictionHistory records.
     */
    List<PredictionHistory> findAllByOrderByPredictedAtDesc();

    /**
     * Counts the total number of a specific prediction result (e.g., "Counterfeit" or "Genuine").
     * Used for generating dashboard statistics.
     * @param prediction The prediction result string.
     * @return The count of records matching that prediction.
     */
    long countByPrediction(String prediction);
}
