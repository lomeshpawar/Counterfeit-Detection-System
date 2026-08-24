package com.counterfeit.repository;

import com.counterfeit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity.
 * JpaRepository provides all basic CRUD (Create, Read, Update, Delete) operations.
 */
@Repository // Indicates that this is a Data Access Object (DAO) component
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     * @param email The email to search for.
     * @return An Optional containing the User if found, or empty if not.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user with the given email exists.
     * @param email The email to check.
     * @return True if it exists, false otherwise.
     */
    Boolean existsByEmail(String email);

    /**
     * Counts the total number of users with a specific role (e.g., 'USER' or 'ADMIN').
     * Useful for an admin dashboard.
     * @param role The role to count.
     * @return The number of users with that role.
     */
    long countByRole(String role);
}
