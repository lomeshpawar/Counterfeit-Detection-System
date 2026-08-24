<<<<<<< HEAD
# 🛡️ CounterCheck — Counterfeit Product Detection System

> **MCA Research Work Project**  
> *AI-Based Counterfeit Product Identification & Verification Platform*

CounterCheck is a full-stack, 3-tier web application designed to detect counterfeit products using Deep Learning (MobileNetV2 Transfer Learning), a robust Spring Boot REST API, a MySQL database, and an intuitive user dashboard.

---

## 🏗️ System Architecture

```
[ Frontend Client ]  <--->  [ Spring Boot REST API ]  <--->  [ Flask AI Service ]
(HTML5 / CSS3 / JS)            (Port 8081)                    (MobileNetV2 / Port 5000)
                                      |
                                      v
                             [ MySQL Database ]
                               (Port 3306)
```

1. **Frontend (`/frontend`)**: Responsive HTML5, CSS3, and Vanilla JavaScript interfaces for registration, login, product analysis upload, user history, and admin dashboard.
2. **Spring Boot Backend (`/java-backend`)**: REST API handling user authentication (BCrypt), multipart image file uploads, MySQL persistence, and HTTP proxying to the AI microservice.
3. **AI Microservice (`/ai-service`)**: Python Flask microservice using PyTorch and MobileNetV2 Transfer Learning to inspect high-frequency print/texture features and determine product authenticity (`Genuine` vs `Counterfeit`).
4. **Database (`/database`)**: MySQL/MariaDB database (`counterfeit_db`) storing relational user accounts and prediction records with foreign key constraints.

---

## 📁 Repository Structure

```
Counterfeit-Product-Detection-System/
├── README.md                          # Project Master Documentation
├── database/
│   └── counterfeit_db.sql             # Complete Database Setup Script
├── java-backend/
│   ├── pom.xml                        # Maven Dependencies (Spring Boot 3.2.5)
│   └── src/main/
│       ├── java/com/counterfeit/
│       │   ├── CounterCheckApplication.java
│       │   ├── config/                # SecurityConfig & WebConfig (/uploads/**)
│       │   ├── controller/            # AuthController & PredictionController
│       │   ├── dto/                   # Request/Response Data Objects
│       │   ├── entity/                # User & PredictionHistory JPA Entities
│       │   ├── repository/            # UserRepository & PredictionHistoryRepository
│       │   └── service/               # UserService & PredictionService
│       └── resources/
│           └── application.properties # Server port 8081 & MySQL DataSources
├── ai-service/
│   ├── app.py                         # Flask REST API Microservice (Port 5000)
│   ├── predict.py                     # MobileNetV2 & Image Feature Analyzer
│   ├── train_model.py                 # PyTorch Model Training & Fine-tuning Script
│   └── requirements.txt               # Python Dependencies (Flask, PyTorch, Pillow)
└── frontend/
    ├── index.html                     # Landing Page
    ├── upload.html                    # Product Image Upload & Analyze Page
    ├── result.html                    # AI Analysis Result Display Page
    ├── history.html                   # Prediction History Table Page
    ├── login.html                     # User Login Page
    ├── register.html                  # User Registration Page
    ├── admin-login.html               # Admin Login Page
    ├── admin-dashboard.html           # Admin Dashboard & System Analytics Page
    ├── css/style.css                  # Modern UI Stylesheet
    └── js/script.js                   # REST API Integration Client Script
```

---

## ⚡ Quick Start Guide

### Step 1: Set Up MySQL Database
1. Open XAMPP or MySQL Workbench and start MySQL on port `3306`.
2. Run the setup script:
   ```bash
   mysql -u root -p < database/counterfeit_db.sql
   ```

### Step 2: Start Flask AI Microservice
1. Navigate to the `ai-service` directory:
   ```bash
   cd ai-service
   ```
2. Install Python dependencies:
   ```bash
   pip install -r requirements.txt
   ```
3. Launch the AI microservice:
   ```bash
   python app.py
   ```
   *The Flask microservice will start on `http://localhost:5000`.*

### Step 3: Start Spring Boot Backend
1. Navigate to the `java-backend` directory:
   ```bash
   cd java-backend
   ```
2. Compile and launch using Maven:
   ```bash
   mvn spring-boot:run
   ```
   *The Spring Boot server will start on `http://localhost:8081`.*

### Step 4: Open Frontend Application
- Serve the `frontend/` directory using VS Code Live Server (or open `frontend/index.html` directly in your web browser).

---

## 🔑 Default Credentials

- **Admin Account**: `admin@countercheck.com` / `password123`
- **Test User 1**: `rahul@example.com` / `password123`
- **Test User 2**: `priya@example.com` / `password123`

---

## 📡 REST API Reference

### Auth Endpoints
- `POST /api/auth/register` — Register new user account.
- `POST /api/auth/login` — Authenticate user credentials.
- `POST /api/auth/admin/login` — Authenticate admin credentials.

### Prediction Endpoints
- `POST /api/predictions/analyze` — Upload multipart image file and get AI prediction.
- `GET /api/predictions/history/{userId}` — Fetch prediction history for a user.
- `GET /api/predictions/stats` — Get total users, predictions, and counterfeit counts.
- `GET /api/predictions/all` — Fetch all predictions (Admin view).
- `DELETE /api/predictions/{id}` — Delete prediction record.

---

## 📜 License & Acknowledgments
Developed as part of the MCA Research Work Project © 2026 CounterCheck.
=======
# Counterfeit Product Detection System

A full-stack project for detecting potentially counterfeit products using an AI service, Java backend, database, and web frontend.

## Project Structure

- `frontend/` — User and admin web pages
- `java-backend/` — Java backend service
- `ai-service/` — Python AI prediction and training service
- `database/` — MySQL database schema

## Getting Started

Configure the required database and dependencies for the Java and Python services, then run the services locally.
>>>>>>> b984e0b10eb9d0752c5edb2e60e2697fbd1a7923
