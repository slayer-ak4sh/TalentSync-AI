# TalentSync AI

An AI-powered resume-to-job matching platform. Upload a resume, add a job description, and get an instant fit score with matched skills, missing skills, and a plain-English gap analysis — all powered by Google Gemini.

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     Browser / UI                        │
│              React + Vite  (port 8080)                  │
└────────────────────┬────────────────────────────────────┘
                     │ HTTP
        ┌────────────┴────────────┐
        ▼                         ▼
┌───────────────┐        ┌─────────────────┐
│ upload-service│        │matching-service │
│  Spring Boot  │◄───────│  Spring Boot    │
│   port 8081   │        │   port 8082     │
└──────┬────────┘        └────────┬────────┘
       │                          │
       ▼                          ▼
┌─────────────┐          ┌────────────────┐
│  resumematcher│        │ matchingservice │
│  (PostgreSQL) │        │  (PostgreSQL)  │
└─────────────┘          └────────────────┘
                                  │
                                  ▼
                        ┌──────────────────┐
                        │  Google Gemini   │
                        │  gemini-3.6-flash│
                        └──────────────────┘
```

Three independently deployable services, each with its own PostgreSQL database, all running in Kubernetes via a single Helm chart.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 19, Vite 8, plain CSS |
| Backend | Spring Boot 4.1, Java 21 |
| Database | PostgreSQL 16 (two separate DBs) |
| AI | Google Gemini API (gemini-3.6-flash) |
| PDF parsing | Apache PDFBox 3 |
| Containerization | Docker, multi-stage builds |
| Orchestration | Kubernetes (kind), Helm 3 |
| CI | GitHub Actions |

---

## Project Structure

```
resume-matcher/
├── frontend/                   # React SPA
│   ├── src/
│   │   ├── App.jsx             # Main UI — all three panels
│   │   └── App.css             # Design system (Space Grotesk, grid layout)
│   ├── Dockerfile              # Node build → Nginx serve
│   ├── nginx.conf              # SPA routing fallback
│   └── .env.production         # Baked-in backend URLs for production build
│
├── upload-service/             # Handles resumes and job descriptions
│   └── src/main/java/
│       ├── controller/         # ResumeController, JobDescriptionController
│       ├── service/            # ResumeService, JobDescriptionService
│       ├── util/               # ResumeTextExtractor (PDF + TXT)
│       └── model/              # Resume, JobDescription entities
│
├── matching-service/           # AI matching engine
│   └── src/main/java/
│       ├── controller/         # MatchController
│       ├── service/            # MatchingService, GeminiClient, PromptBuilder
│       ├── client/             # UploadServiceClient (inter-service HTTP)
│       └── model/              # MatchResult entity (jsonb skill arrays)
│
├── resume-matcher-chart/       # Helm chart — deploys all 6 pods
│   ├── templates/
│   │   ├── frontend-deployment.yaml
│   │   ├── matching-deployment.yaml
│   │   ├── upload-deployment.yaml
│   │   └── postgres-statefulset.yaml
│   └── values.yaml
│
├── docker-compose.yml          # Local Docker dev (no Kubernetes needed)
├── init-multi-db.sh            # Creates both PostgreSQL databases on first run
└── .github/workflows/ci.yml   # CI pipeline
```

---

## API Reference

### upload-service — `http://localhost:8081`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/resumes` | Upload resume (multipart, PDF or TXT) |
| `GET` | `/api/v1/resumes` | List all resumes |
| `GET` | `/api/v1/resumes/{id}` | Get resume by ID |
| `POST` | `/api/v1/jobs` | Create job description (JSON) |
| `GET` | `/api/v1/jobs` | List all job descriptions |
| `GET` | `/api/v1/jobs/{id}` | Get job by ID |
| `GET` | `/actuator/health` | Health check |

### matching-service — `http://localhost:8082`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/match` | Run match (JSON body: `resumeId`, `jobId`) |
| `GET` | `/api/v1/match/run?resumeId=1&jobId=1` | Run match via query params |
| `GET` | `/api/v1/match/{id}` | Get match result by ID |
| `GET` | `/api/v1/match/resume/{resumeId}` | All matches for a resume |
| `GET` | `/api/v1/match/job/{jobId}` | All matches for a job |
| `POST` | `/api/v1/match/bulk` | Match one resume against multiple jobs |
| `GET` | `/actuator/health` | Health check |

### Match response shape

```json
{
  "resumeId": 1,
  "jobId": 1,
  "fitScore": 78,
  "matchedSkills": ["Java", "Spring Boot", "PostgreSQL"],
  "missingSkills": ["Kubernetes", "Terraform"],
  "gapAnalysis": "Strong backend fit. Missing infrastructure/IaC experience.",
  "createdAt": "2026-08-09T14:23:56"
}
```

---

## Prerequisites

- Docker
- Java 21 + Maven (for local backend dev only)
- Node 20 (for local frontend dev only)
- kubectl + kind (for Kubernetes deployment)
- Helm 3 (for Kubernetes deployment)
- A Google Gemini API key — get one at https://aistudio.google.com

---

## Running Locally

### Option A — Docker Compose (simplest)

```bash
# 1. Clone the repo
git clone https://github.com/slayer-ak4sh/TalentSync-AI.git
cd TalentSync-AI

# 2. Set your credentials
cp .env.example .env
# Edit .env and set GEMINI_API_KEY=your_key_here

# 3. Start everything
docker compose up --build

# 4. Open the frontend dev server
cd frontend
npm install
npm run dev
```

Open http://localhost:5173

Services available at:
- upload-service: http://localhost:8081
- matching-service: http://localhost:8082

### Option B — Kubernetes via kind + Helm

```bash
# 1. Create the cluster
kind create cluster --name resume-matcher

# 2. Build and load all images
docker build -t upload-service:local ./upload-service
docker build -t matching-service:local ./matching-service
docker build -t frontend:local ./frontend

kind load docker-image upload-service:local --name resume-matcher
kind load docker-image matching-service:local --name resume-matcher
kind load docker-image frontend:local --name resume-matcher

# 3. Deploy with Helm
cd resume-matcher-chart
helm upgrade --install resume-matcher . \
  --set geminiApiKey=YOUR_GEMINI_API_KEY \
  --namespace resume-matcher \
  --create-namespace

# 4. Port-forward all three services
kubectl port-forward svc/frontend 8080:80 -n resume-matcher &
kubectl port-forward svc/upload-service 8081:8081 -n resume-matcher &
kubectl port-forward svc/matching-service 8082:8082 -n resume-matcher &
```

Open http://localhost:8080

---

## Environment Variables

### `.env` (Docker Compose — never commit)

| Variable | Description |
|---|---|
| `GEMINI_API_KEY` | Your Google Gemini API key |
| `POSTGRES_PASSWORD` | PostgreSQL root password |
| `DB_USER` | Database username |
| `DB_PASSWORD` | Database password |

### Helm (pass via `--set`, never store in `values.yaml`)

```bash
helm upgrade --install resume-matcher . \
  --set geminiApiKey=YOUR_KEY \
  --namespace resume-matcher
```

---

## How the AI Matching Works

1. User selects a resume and a job description in the UI
2. matching-service fetches the raw text of both from upload-service
3. A structured prompt is sent to Gemini asking for JSON output with four fields: `matchedSkills`, `missingSkills`, `fitScore`, `gapAnalysis`
4. The response is parsed, persisted to PostgreSQL (with skill arrays stored as `jsonb`), and returned to the UI
5. Results are cached — re-running the same `resumeId` + `jobId` pair returns the stored result instantly

---

## CI Pipeline

GitHub Actions runs on every push and pull request to `main`:

```
test-upload-service ──┐
                       ├──► build-docker-images
test-matching-service ─┤     (all three images)
                       │
build-frontend ────────┘
```

- `test-upload-service` — spins up Postgres, runs `mvn test`, builds jar
- `test-matching-service` — same with `GEMINI_API_KEY=dummy-key-for-ci`
- `build-frontend` — `npm ci` + `npm run build`
- `build-docker-images` — only runs after all three pass; builds all three Docker images

---

## Key Design Decisions

- Two separate PostgreSQL databases (`resumematcher`, `matchingservice`) — services own their data, no shared schema
- `matchedSkills` and `missingSkills` stored as `jsonb` in PostgreSQL — queryable arrays without a join table
- Gemini prompt instructs the model to return raw JSON only (no markdown fences) — `stripMarkdownFences()` handles cases where the model ignores this
- Match results are cached by `(resumeId, jobId)` — avoids redundant Gemini API calls for the same pair
- Vite dev proxy (`/upload` → 8081, `/match` → 8082) — eliminates CORS issues during local development without touching backend config
- `imagePullPolicy: Never` in Kubernetes — uses locally loaded images, no registry needed for local demo

---

## License

MIT
