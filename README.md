# 🛡️ CounterCheck — Counterfeit Product Detection System

> **MCA Research Work Project**  
> **AI-Based Product Authenticity Analysis Platform**

CounterCheck is a full-stack web application for analyzing product images and identifying potentially counterfeit products. The system combines a responsive web frontend, a Spring Boot REST API, a Python/Flask AI service, and a MySQL database.

## ✨ Features

- User registration and login
- Admin login and dashboard
- Product image upload and analysis
- AI-assisted authenticity prediction
- Prediction history
- System statistics and analytics
- MySQL-based persistence

## 🏗️ Architecture

```text
Frontend (HTML/CSS/JavaScript)
          │
          ▼
Spring Boot REST API
          ├──────────────► MySQL Database
          │
          ▼
Flask AI Service
```

## 📁 Repository Structure

```text
Counterfeit-Detection-System/
├── frontend/          # User and admin web interfaces
├── java-backend/      # Spring Boot REST API
├── ai-service/        # Flask AI service and model utilities
├── database/          # MySQL database schema
└── README.md          # Project documentation
```

## 🛠️ Technology Stack

| Layer | Technologies |
|---|---|
| Frontend | HTML5, CSS3, JavaScript |
| Backend | Java, Spring Boot, Maven |
| AI Service | Python, Flask, PyTorch |
| Database | MySQL |

## 🚀 Getting Started

### 1. Database
Start MySQL and import the SQL file from the `database/` directory.

### 2. AI Service

```bash
cd ai-service
pip install -r requirements.txt
python app.py
```

### 3. Java Backend

```bash
cd java-backend
mvn spring-boot:run
```

### 4. Frontend
Serve the `frontend/` directory with a local development server such as VS Code Live Server.

## 📡 Main API Areas

- Authentication and user management
- Product image analysis
- Prediction history
- System statistics
- Admin data access

## ⚠️ Security Note

Do not commit real passwords, API keys, database credentials, trained model secrets, or personal data to this repository. Use local configuration or environment variables for sensitive values.

## 📌 Project Status

This repository is under active development. Contributions, bug reports, and improvements should be tracked through GitHub issues and pull requests.

---

Developed as an MCA Research Work Project.