# CounterCheck — Counterfeit Product Detection System

> Full-stack MCA research project for AI-assisted product authenticity analysis using a Java backend and Python image-analysis service.

## 📌 Overview

CounterCheck is a multi-service web application that allows users to submit product images for authenticity analysis and review prediction history.

The project separates responsibilities across a **Java/Spring Boot REST API**, **Python/Flask AI service**, **MySQL database**, and **HTML/CSS/JavaScript frontend**.

> **Academic scope:** This is a research/prototype system. Its real-world counterfeit-detection effectiveness should be evaluated using measured model metrics and representative datasets before making production or commercial claims.

## ✨ Key Features

- User registration and login
- Admin authentication and dashboard
- Product image upload
- AI-assisted image analysis
- Prediction history
- System statistics and analytics
- MySQL persistence
- Docker Compose support
- GitHub Pages frontend deployment
- GitHub Actions automation and security checks

## 🏗️ Architecture

```text
                    ┌──────────────────────┐
                    │  HTML / CSS / JS     │
                    │  GitHub Pages        │
                    └──────────┬───────────┘
                               │ HTTP
                               ▼
                    ┌──────────────────────┐
                    │ Spring Boot REST API │
                    │ Java / Maven         │
                    └───────┬────────┬─────┘
                            │        │
                            ▼        ▼
                     ┌──────────┐  ┌──────────────┐
                     │  MySQL   │  │ Flask AI     │
                     │ Database │  │ Python/PyTorch│
                     └──────────┘  └──────────────┘
```

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| Frontend | HTML5, CSS3, JavaScript |
| Backend | Java, Spring Boot, Maven |
| AI Service | Python, Flask, PyTorch |
| Database | MySQL |
| Deployment | GitHub Pages, GitHub Actions |
| Local Infrastructure | Docker Compose |
| API Testing | REST API tooling / Postman |

## 📁 Repository Structure

```text
Counterfeit-Detection-System/
├── frontend/
├── java-backend/
├── ai-service/
├── database/
├── .github/workflows/
├── .env.example
├── docker-compose.yml
├── CONTRIBUTING.md
├── SECURITY.md
└── README.md
```

## 🌐 Frontend

The static frontend is published through GitHub Pages:

**https://lomeshpawar.github.io/Counterfeit-Detection-System/**

GitHub Pages hosts the frontend only. The Java API, Python AI service, and MySQL database are separate components.

## 🚀 Run Locally

### Prerequisites

- Java 17+
- Python 3.x
- MySQL
- Maven
- Docker Desktop (optional)

### Docker

```bash
cp .env.example .env
# Configure local values in .env
docker compose up --build -d
```

### AI Service

```bash
cd ai-service
pip install -r requirements.txt
python app.py
```

### Java Backend

```bash
cd java-backend
mvn spring-boot:run
```

## 🔐 Security

- Never commit passwords, API keys, tokens, or real credentials.
- Keep local secrets in environment variables.
- Do not commit `.env` files.
- Review dependency and security-scan failures before merging.

## 📊 Evaluation

For academic evaluation, document:

- Dataset source and class distribution
- Train/validation/test split
- Model architecture
- Accuracy
- Precision
- Recall
- F1-score
- Confusion matrix
- Limitations and failure cases

Only report metrics that were actually measured.

## 🔮 Roadmap

- Expand unit and integration tests
- Add reproducible AI evaluation
- Improve API validation and error handling
- Add structured logging and observability
- Deploy Java and AI services together in a production environment
- Add OpenAPI/Swagger documentation
- Improve architecture and API diagrams

## 👨‍💻 Author

**Lomesh Pawar** — MCA Student | Java Backend Developer

[GitHub](https://github.com/lomeshpawar)

## 📄 License

No open-source license is currently declared. Until a license is added, assume the source is all rights reserved.
