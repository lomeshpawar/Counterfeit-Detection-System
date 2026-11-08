# CounterCheck — Counterfeit Product Detection System

> A full-stack academic research project exploring experimental image-based product analysis. It is not a validated counterfeit classifier.

CounterCheck combines a static HTML/CSS/JavaScript interface, a Spring Boot REST API, a Python AI service, and MySQL persistence. The repository separates those components so they can be developed and tested independently.

## Frontend demo

**Static UI:** https://lomeshpawar.github.io/Counterfeit-Detection-System/

**Frontend deployment workflow:** https://github.com/lomeshpawar/Counterfeit-Detection-System/actions/workflows/pages.yml

GitHub Pages hosts only the static frontend. Interactive features that require authentication, image analysis, prediction history, or administrative data need the Java API, AI service, and database to be deployed and correctly configured. The current JavaScript still points to `http://localhost:8081/api` by default, so those requests will not reach your local computer when a recruiter opens the public Pages site. Treat the public URL as a UI preview until a public API URL is configured.

## Key capabilities

- User registration and login interface
- Product image selection, preview, and submission for analysis
- Prediction result and history views
- Administrative views and prediction statistics
- Spring Boot REST API
- Separate Python AI service
- MySQL persistence
- GitHub Actions checks and GitHub Pages frontend deployment

These are prototype capabilities and depend on the relevant services being configured and running; the frontend URL alone does not provide a hosted inference backend.

## Known limitations

The current `main` branch is an experimental prototype and has important security and model-correctness limitations:

- **Authentication/authorization:** Spring Security currently permits all requests. The registration/login and administrative screens must not be interpreted as proof of authenticated sessions or server-enforced role-based access. Client-supplied identity or administrator headers are not trusted credentials. See [issue #15](https://github.com/lomeshpawar/Counterfeit-Detection-System/issues/15).
- **AI-service failure handling:** The backend now fails closed when the AI service is unavailable or returns an invalid response. It returns a retryable service-unavailable response and does not persist a synthetic Genuine/Counterfeit result. The AI service's successful output is still experimental and is not a validated counterfeit classification.
- **Model alignment:** The training script saves a custom MobileNetV2 checkpoint, but the current inference path does not load that checkpoint. It uses generic ImageNet outputs with image-quality heuristics. The resulting score is not a validated or calibrated counterfeit probability. See [issue #31](https://github.com/lomeshpawar/Counterfeit-Detection-System/issues/31).
- **Uploaded-image privacy:** The API validates image content and dimensions and stores files under generated names, but `WebConfig` still exposes `/uploads/**` as a public static-resource path without authenticated ownership checks. Do not upload private/customer images. See [issue #35](https://github.com/lomeshpawar/Counterfeit-Detection-System/issues/35) and [follow-up issue #41](https://github.com/lomeshpawar/Counterfeit-Detection-System/issues/41).
- **Deployment:** Full functionality requires a separately deployed/configured Java API, Python service, and database. GitHub Pages alone is only a static UI preview.

Do not use this prototype to make real-world product-authenticity, purchasing, safety, or enforcement decisions.

## Architecture

```text
Browser / GitHub Pages
        |
        | HTTPS API requests (configure public API URL)
        v
Spring Boot REST API  <---->  MySQL
        |
        v
Python AI service
```

## Technology stack

| Layer | Technology |
|---|---|
| Frontend | HTML5, CSS3, JavaScript |
| Backend API | Java, Spring Boot, Maven |
| AI service | Python, Flask / model-serving code |
| Database | MySQL |
| Static hosting | GitHub Pages |
| Automation | GitHub Actions |
| Local multi-service setup | Docker Compose |

## Repository layout

```text
Counterfeit-Detection-System/
├── frontend/              # Static pages, styles, and browser JavaScript
├── java-backend/           # Spring Boot API
├── ai-service/             # Python AI service
├── database/               # Database scripts
├── .github/workflows/      # CI, security, and Pages workflows
├── .env.example            # Configuration template
├── docker-compose.yml      # Local service orchestration
├── CONTRIBUTING.md
├── SECURITY.md
└── README.md
```

## Run locally

### Option A — Docker Compose

1. Install Docker Desktop.
2. Copy the example environment file:

   ```powershell
   Copy-Item .env.example .env
   ```

3. Edit `.env` and set unique local values for `MYSQL_PASSWORD` and `MYSQL_ROOT_PASSWORD`. The example intentionally leaves both blank, and Docker Compose will refuse to start until you configure them. Use a dedicated non-root `MYSQL_USER` for the application.

4. Start the services:

   ```bash
   docker compose up --build
   ```

Review `docker-compose.yml` and `.env.example` for the exact ports and required variables before starting.

### Option B — Run components separately

1. Start MySQL with a dedicated application account and explicit `DB_USERNAME` / `DB_PASSWORD` values; do not use the root account for the application. Apply the SQL scripts under `database/`.
2. Start the Python service using its requirements file and documented entry point.
3. Start the Java backend from `java-backend/` with `mvn spring-boot:run`.
4. Serve the `frontend/` directory with VS Code Live Server or another static server.

The frontend's API base URL is currently hard-coded to `http://localhost:8081/api` in `frontend/js/script.js`. For a public deployment, change it to the deployed API's HTTPS base URL and confirm backend CORS and upload-storage configuration. Never put database credentials, passwords, tokens, or private keys in frontend code.

## GitHub Pages deployment

The workflow `.github/workflows/pages.yml` publishes `frontend/` as a static site. After changes are pushed to `main`, inspect the repository's **Actions** tab and confirm the latest **Deploy Frontend to GitHub Pages** run succeeds.

## Automated tests and validation

Run the checks from the repository root. They match the commands used by the current GitHub Actions workflows.

**Java backend:**

```bash
cd java-backend
mvn -B verify
```

**Python AI service:**

```bash
cd ai-service
python -m pip install -r requirements.txt
python -m pytest -q
python -m py_compile app.py predict.py train_model.py
```

The dedicated AI Service CI workflow also audits the pinned Python requirements and runs the tests under `ai-service/tests/`:

```bash
python -m pip install pip-audit
pip-audit -r requirements.txt
python -m pytest -q tests
```

These checks validate the current automated backend and Python test suites, Python source compilation, and the dependency audit. They do **not** provide browser end-to-end coverage or establish counterfeit-detection accuracy. Review the latest workflow runs in the [Actions tab](https://github.com/lomeshpawar/Counterfeit-Detection-System/actions) for the result on the commit you intend to use.

## Validation and limitations

- CI runs automated repository checks; review the Actions tab for the latest result rather than assuming every workflow is green.
- Verify image uploads, CORS, API routing, persistent uploaded-image storage, and backend availability before calling the application fully live.
- Model metrics and real-world counterfeit-detection accuracy should only be reported when supported by a repeatable evaluation on a documented dataset.
- Do not upload sensitive product/customer data to a public demo.

## Security

- Keep `.env` local and never commit real credentials.
- Use HTTPS for public API endpoints.
- Validate uploaded file type and size on the server as well as in the browser.
- Restrict administrative operations in the backend; frontend checks alone are not security controls.

## Project status

Academic full-stack project under development. The static frontend has a GitHub Pages workflow; full online functionality depends on the API, AI service, database, and public API configuration.

## Author

**Lomesh Pawar** · [GitHub](https://github.com/lomeshpawar)
