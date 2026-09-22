# CounterCheck — Counterfeit Product Detection System

> Full-stack engineering project for image-based counterfeit product analysis using a Java REST backend and a Python AI service.

CounterCheck demonstrates a practical multi-service workflow: users submit product images through a web interface, the Java/Spring Boot API coordinates application logic and persistence, and a Python service handles image-analysis requests.

## Engineering Highlights

- Spring Boot REST API with Java 17
- Python/Flask AI service
- MySQL persistence with JPA/Hibernate
- Image upload and prediction-history workflow
- Separate frontend, backend, AI, and database components
- Docker Compose support for local multi-service development
- Automated Java/Python validation through GitHub Actions
- Repository security checks and secret scanning

> **Evidence note:** This repository documents implemented capabilities only. Model accuracy, production readiness, and real-world detection effectiveness are not claimed without measured evaluation.

## Architecture

```text
Browser / Frontend
       │ HTTP
       ▼
Spring Boot REST API ───────► MySQL
       │
       ▼
Python / Flask AI Service
       │
       ▼
Image analysis / prediction
```

## Technology Stack

| Component | Technology |
|---|---|
| Frontend | HTML5, CSS3, JavaScript |
| Backend | Java 17, Spring Boot, Spring Data JPA, Maven |
| Security / validation | Spring Security, Spring Validation |
| AI service | Python, Flask, PyTorch, Pillow |
| Database | MySQL 8 |
| Local deployment | Docker Compose |
| CI | GitHub Actions |

## Repository Structure

```text
Counterfeit-Detection-System/
├── frontend/                 # Web interface
├── java-backend/             # Spring Boot REST API
├── ai-service/               # Flask AI service and model code
├── database/                 # Database initialization scripts
├── .github/workflows/        # CI and security automation
├── .env.example              # Safe local configuration template
├── docker-compose.yml        # Multi-service local deployment
├── CONTRIBUTING.md           # Development workflow
├── SECURITY.md               # Security reporting guidance
└── README.md
```

## Quick Start

### Prerequisites

- Java 17
- Maven
- Python 3.11+
- MySQL 8+
- Docker Desktop (recommended for the multi-service setup)

### Docker Compose

Copy the configuration template:

```bash
cp .env.example .env
```

Replace placeholder credentials in `.env`, then:

```bash
docker compose up --build -d
```

The backend is exposed on port `8081`.

Stop the services:

```bash
docker compose down
```

### Manual Development

1. Start MySQL and initialize the scripts under `database/`.
2. Start the AI service:

```bash
cd ai-service
python -m pip install -r requirements.txt
python app.py
```

3. Start the backend:

```bash
cd java-backend
mvn spring-boot:run
```

4. Serve `frontend/` with a local development server.

## Configuration

The backend reads sensitive and machine-specific values from environment variables, including:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `UPLOAD_DIR`
- `CORS_ALLOWED_ORIGINS`

Use `.env.example` as the template. Never commit `.env`, real passwords, API keys, tokens, or uploaded user data.

## Testing & CI

GitHub Actions validates the backend and AI service:

- Maven build and test execution
- Python dependency installation
- Python test execution
- Python source compilation
- Dependency-review checks on pull requests
- Repository secret scanning

Run component checks locally before submitting a change:

```bash
cd java-backend
mvn -B verify
```

```bash
cd ai-service
pytest -q
python -m py_compile app.py predict.py train_model.py
```

Only treat a check as passing when it has actually been executed successfully.

## Security

- Keep secrets outside Git.
- Use environment variables for local credentials.
- Do not upload real personal data or credentials.
- Review CI security failures before merging.
- Report suspected vulnerabilities privately using `SECURITY.md`.

## Current Status

The project is maintained as an MCA research-work project focused on full-stack software engineering and AI-assisted product authenticity analysis.

Known improvement areas include broader automated tests, reproducible model evaluation, stronger API validation, observability, and deployment hardening.

## Roadmap

- Expand unit and integration test coverage
- Document reproducible AI evaluation and measured metrics
- Improve API validation and error responses
- Add structured logging and observability
- Strengthen deployment documentation
- Add architecture/API diagrams as the system evolves

These are future improvements, not claims of completed functionality.

## Contributing

See `CONTRIBUTING.md` for the development workflow. Keep changes focused, use meaningful commit messages, and include relevant validation evidence.

## License

No open-source license is currently declared. Until a license is added, the repository should be treated as **all rights reserved**.

---

Built as an MCA research-work project with a focus on Java backend engineering, AI-assisted image analysis, and practical full-stack architecture.
