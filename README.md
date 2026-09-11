# CounterCheck — Counterfeit Product Detection System

> Full-stack research project for AI-assisted product authenticity analysis.

CounterCheck is a multi-service web application that lets users submit product images for authenticity analysis and review prediction history. The system separates the web interface, Java backend, Python AI service, and MySQL persistence layer so each component has a clear responsibility.

## Why this project?

Counterfeit products create a difficult verification problem: visual evidence must be processed, an analysis result must be returned through an API, and application data must be stored reliably. This project explores that workflow through a practical full-stack architecture combining Java backend development with an AI service.

## Key capabilities

- User registration and login
- Admin authentication and dashboard
- Product image upload and AI-assisted analysis
- Prediction history
- System statistics and analytics
- MySQL persistence
- Docker Compose support for local multi-service deployment

> **Scope note:** The repository documents the capabilities currently present in the codebase. Model accuracy, production performance, and real-world counterfeit-detection effectiveness have not been claimed without measured evidence.

## Architecture

```text
┌──────────────────────────┐
│   Frontend               │
│   HTML / CSS / JavaScript│
└────────────┬─────────────┘
             │ HTTP
             ▼
┌──────────────────────────┐
│   Spring Boot API        │
│   Java / REST            │
└───────┬───────────┬──────┘
        │           │
        │           └──────────────┐
        ▼                          ▼
┌──────────────────┐      ┌──────────────────┐
│ MySQL Database   │      │ Flask AI Service │
│ Persistence      │      │ Image analysis   │
└──────────────────┘      └──────────────────┘
```

## Technology stack

| Layer | Technology |
|---|---|
| Frontend | HTML5, CSS3, JavaScript |
| Backend | Java, Spring Boot, Maven |
| AI service | Python, Flask, PyTorch |
| Database | MySQL |
| Local deployment | Docker Compose |

## Repository structure

```text
Counterfeit-Detection-System/
├── frontend/             # Web interface
├── java-backend/          # Spring Boot REST API
├── ai-service/            # Flask AI service and model code
├── database/              # Database schema/scripts
├── .github/workflows/     # CI and security automation
├── .env.example           # Configuration template
├── docker-compose.yml     # Local multi-service deployment
├── CONTRIBUTING.md        # Contribution workflow
├── SECURITY.md            # Security reporting guidance
└── README.md              # Project documentation
```

## Quick start with Docker

The repository includes a Docker Compose configuration for local multi-service deployment.

```bash
cp .env.example .env
# Edit .env and replace placeholder values with local values
docker compose up --build -d
```

The Spring Boot backend is configured for port `8081`. Do not commit `.env` or real credentials.

To stop the local services:

```bash
docker compose down
```

## Development setup

### 1. Database

Start a local MySQL instance and apply the SQL scripts available under `database/`.

### 2. AI service

```bash
cd ai-service
pip install -r requirements.txt
python app.py
```

### 3. Java backend

```bash
cd java-backend
mvn spring-boot:run
```

### 4. Frontend

Serve the `frontend/` directory with a local development server, such as VS Code Live Server.

### Configuration

Use `.env.example` as the starting point for local configuration. Keep secrets, passwords, API keys, and machine-specific settings outside Git.

## API areas

The backend exposes functionality covering:

- Authentication and user management
- Product image analysis
- Prediction history
- System statistics
- Administrative data access

For endpoint-level details, see the Spring Boot source under `java-backend/`.

## Testing and quality

The project uses GitHub Actions for automated repository checks and security scanning. Python validation is maintained separately from the application services where appropriate.

Before submitting a change, run the relevant checks for the component you modified. Do not describe a test as passing unless it has actually been executed.

## Security

Security is treated as part of the development workflow.

- Never commit real credentials or API keys.
- Store local secrets in environment variables or another local secret-management mechanism.
- Keep `.env` files out of version control.
- Review dependency and secret-scan failures before merging changes.
- Report suspected vulnerabilities privately according to `SECURITY.md`.

## Current status

This project is under active development as an MCA research-work project. The architecture is intended to provide a clear foundation for further improvements in model evaluation, automated testing, observability, and deployment.

## Roadmap

Potential engineering improvements include:

- Expand automated unit and integration test coverage
- Add reproducible AI evaluation with documented datasets and metrics
- Improve API validation and error handling
- Add stronger observability and structured logging
- Improve deployment documentation
- Add architecture and API diagrams as the system evolves

These are roadmap items, not claims that the functionality already exists.

## Contributing

Please read `CONTRIBUTING.md` before opening a pull request. Prefer small, focused changes with clear commit messages and relevant validation.

## Security reporting

Please use the process described in `SECURITY.md` for security-related reports. Do not publish credentials or sensitive vulnerability details in public issues.

## License

No license is currently declared in the repository. Until a license is added, assume the source is **all rights reserved** and do not redistribute it as open-source software.

---

Built as an MCA Research Work Project with a focus on full-stack software engineering and AI-assisted product authenticity analysis.
