package com.counterfeit.dto;

/**
 * Data Transfer Object (DTO) for Dashboard Statistics.
 * This class holds summary data that might be displayed on an admin dashboard.
 */
public class DashboardStats {

    private long totalUsers;       // Total number of registered users
    private long totalPredictions; // Total number of predictions made across the system
    private long genuineCount;     // Total number of products predicted as genuine
    private long counterfeitCount; // Total number of products predicted as counterfeit

    // Constructor
    public DashboardStats(long totalUsers, long totalPredictions, long genuineCount, long counterfeitCount) {
        this.totalUsers = totalUsers;
        this.totalPredictions = totalPredictions;
        this.genuineCount = genuineCount;
        this.counterfeitCount = counterfeitCount;
    }

    // Default constructor
    public DashboardStats() {
    }

    // Getters and Setters

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalPredictions() {
        return totalPredictions;
    }

    public void setTotalPredictions(long totalPredictions) {
        this.totalPredictions = totalPredictions;
    }

    public long getGenuineCount() {
        return genuineCount;
    }

    public void setGenuineCount(long genuineCount) {
        this.genuineCount = genuineCount;
    }

    public long getCounterfeitCount() {
        return counterfeitCount;
    }

    public void setCounterfeitCount(long counterfeitCount) {
        this.counterfeitCount = counterfeitCount;
    }
}
