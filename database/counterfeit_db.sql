-- ============================================================
-- CounterCheck - Counterfeit Product Detection System
-- Database Setup Script
-- ============================================================
-- Author  : Lomesh Pawar
-- Project : MCA Research Work
-- Date    : August 2026
-- ============================================================


-- ============================================================
-- STEP 1: Create the Database
-- ============================================================
-- This creates a new database called 'counterfeit_db'.
-- IF NOT EXISTS ensures no error if the database already exists.

CREATE DATABASE IF NOT EXISTS counterfeit_db;

-- Switch to using our new database
USE counterfeit_db;


-- ============================================================
-- STEP 2: Create the 'users' Table
-- ============================================================
-- This table stores all registered users (both regular users
-- and admins). Each user has a unique email.
--
-- Columns explained:
--   id              → Auto-generated unique number for each user
--   name            → User's full name (from register form)
--   email           → User's email address (must be unique)
--   password        → Hashed password (Spring Boot will hash it)
--   role            → Either 'USER' or 'ADMIN'
--   created_at      → When the account was created (auto-filled)

CREATE TABLE IF NOT EXISTS users (

    id          BIGINT          AUTO_INCREMENT  PRIMARY KEY,
    name        VARCHAR(100)    NOT NULL,
    email       VARCHAR(150)    NOT NULL        UNIQUE,
    password    VARCHAR(255)    NOT NULL,
    role        VARCHAR(10)     NOT NULL        DEFAULT 'USER',
    created_at  TIMESTAMP       NOT NULL        DEFAULT CURRENT_TIMESTAMP

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================================
-- STEP 3: Create the 'prediction_history' Table
-- ============================================================
-- This table stores every product analysis result.
-- It is linked to the 'users' table through the 'user_id' column
-- (this is called a Foreign Key relationship).
--
-- Columns explained:
--   id              → Auto-generated unique number for each prediction
--   user_id         → Which user made this prediction (links to users.id)
--   image_name      → Original filename of the uploaded image
--   image_path      → Where the image is stored on the server
--   product_category→ Category selected during upload (electronics, etc.)
--   prediction      → AI result: 'Genuine' or 'Counterfeit'
--   confidence      → How confident the AI is (e.g., 94.56)
--   model_used      → Which AI model was used (e.g., MobileNetV2)
--   predicted_at    → When the prediction was made (auto-filled)
--
-- ON DELETE CASCADE means:
--   If a user is deleted, all their predictions are also deleted.

CREATE TABLE IF NOT EXISTS prediction_history (

    id                  BIGINT          AUTO_INCREMENT  PRIMARY KEY,
    user_id             BIGINT          NULL,
    image_name          VARCHAR(255)    NOT NULL,
    image_path          VARCHAR(500)    NOT NULL,
    product_category    VARCHAR(50)     DEFAULT 'other',
    prediction          VARCHAR(20)     NOT NULL,
    confidence          DECIMAL(5,2)    NOT NULL,
    model_used          VARCHAR(100)    DEFAULT 'MobileNetV2',
    predicted_at        TIMESTAMP       NOT NULL        DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Key: Links each prediction to a user
    CONSTRAINT fk_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================================
-- STEP 4: Create Indexes for Faster Queries
-- ============================================================
-- Indexes make searching faster. Think of them like an index
-- in a textbook — you can jump directly to what you need.

-- Speed up: "Find all predictions by a specific user"
CREATE INDEX idx_prediction_user
    ON prediction_history(user_id);

-- Speed up: "Find all predictions of a certain type"
CREATE INDEX idx_prediction_result
    ON prediction_history(prediction);

-- Speed up: "Sort predictions by date"
CREATE INDEX idx_prediction_date
    ON prediction_history(predicted_at);


-- ============================================================
-- STEP 6: Insert a Default Admin User
-- ============================================================
-- This creates a default admin account so you can log in to
-- the admin dashboard immediately after setup.
--
-- NOTE: The password below is 'admin123' hashed using BCrypt.
-- Spring Boot Security uses BCrypt to hash passwords.
-- You can change the password later from the application.

INSERT INTO users (name, email, password, role)
VALUES (
    'Admin',
    'admin@countercheck.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN'
);


-- ============================================================
-- STEP 7: Verify Everything Works — Test Queries
-- ============================================================

-- Test 1: Show all users
SELECT * FROM users;

-- Test 2: Show all predictions
SELECT * FROM prediction_history;

-- Test 3: Show predictions with user names (JOIN query)
--          This is exactly what the admin dashboard will use.
SELECT
    ph.id,
    u.name          AS user_name,
    ph.image_name,
    ph.prediction,
    ph.confidence,
    ph.predicted_at
FROM prediction_history ph
JOIN users u ON ph.user_id = u.id
ORDER BY ph.predicted_at DESC;

-- Test 4: Count total genuine vs counterfeit predictions
--          This is what the admin dashboard stats cards need.
SELECT
    prediction,
    COUNT(*) AS total_count
FROM prediction_history
GROUP BY prediction;

-- Test 5: Get prediction history for a specific user (e.g., Rahul)
SELECT
    ph.id,
    ph.image_name,
    ph.prediction,
    ph.confidence,
    ph.predicted_at
FROM prediction_history ph
WHERE ph.user_id = 2
ORDER BY ph.predicted_at DESC;
