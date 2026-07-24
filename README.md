# ⚡ PR Reviewer — AI-Powered GitHub Code Review

![Java 21](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen.svg)
![React](https://img.shields.io/badge/React-18-blue.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Neon-blue.svg)
![Gemini AI](https://img.shields.io/badge/Gemini%20AI-Google-blueviolet.svg)
![GitHub API](https://img.shields.io/badge/GitHub%20API-REST-black.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)

An automated code review system that analyzes GitHub Pull Requests using Google Gemini and posts inline comments directly on the PR.

---

## 🚀 How It Works

**What problem does this solve?**
Code reviews take time and context. PR Reviewer automatically acts as a first-pass reviewer, instantly analyzing changed code, detecting bugs, and leaving actionable inline comments before a human ever has to look at it.

```
Developer opens PR → GitHub Webhook → Verify Signature
  → Fetch PR + Diff → Build Context → Call Gemini AI
  → Parse JSON → Store Review → Post Inline Comments to GitHub PR
```

## 🎥 Demo
*(Add a 2-3 minute demo GIF/Video showing OAuth Login, Repo Selection, Webhook Receipt, and GitHub Comments here)*

---

## 🛠 Tech Stack

| Layer      | Technology                          |
|------------|-------------------------------------|
| Backend    | Java 21, Spring Boot 3.3, Maven     |
| Frontend   | React 18, Vite, Tailwind CSS, Axios |
| Database   | PostgreSQL (Neon), Flyway, JPA      |
| AI         | Google Gemini API                   |
| Auth       | GitHub OAuth2                       |
| Deploy     | Render (backend), Vercel (frontend) |

---

## 📦 Project Structure

```
pr-reviewer/
├── src/main/java/com/prreviewer/
│   ├── auth/           # OAuth success handler, current user endpoint
│   ├── github/         # GitHub REST client (PR, diff, comments)
│   ├── webhook/        # Webhook receiver + signature verification
│   ├── review/         # Context builder, Gemini integration, orchestration
│   ├── controller/     # Thin REST controllers
│   ├── service/        # Business logic
│   ├── repository/     # Spring Data JPA repositories
│   ├── model/          # JPA entities
│   ├── dto/            # Request/response DTOs
│   ├── config/         # Security, CORS, RestClient, AppProperties
│   └── exception/      # Domain exceptions + GlobalExceptionHandler
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/   # Flyway SQL migrations
├── frontend/
│   ├── src/
│   │   ├── api/        # Axios configuration
│   │   ├── components/ # Shared UI components
│   │   ├── hooks/      # React hooks (useAuth)
│   │   └── pages/      # Login, Dashboard, Repositories, Reviews, Settings
│   ├── tailwind.config.js
│   └── vite.config.js
└── .env.example
```

## 📄 Documentation

For a deep dive into the system, see the following files:
- [Architecture Overview](ARCHITECTURE.md)
- [API Reference](API.md)
- [Phase 1 Completion Criteria](PHASE1_CRITERIA.md)
- [Future Enhancements Roadmap](FUTURE_ENHANCEMENTS.md)

---

## ⚙️ Environment Variables

Copy `.env.example` to `.env` and fill in real values:

| Variable               | Description                                |
|------------------------|--------------------------------------------|
| `DATABASE_URL`         | PostgreSQL JDBC URL (Neon)                 |
| `DATABASE_USERNAME`    | Database username                          |
| `DATABASE_PASSWORD`    | Database password                          |
| `GITHUB_CLIENT_ID`     | GitHub OAuth App Client ID                 |
| `GITHUB_CLIENT_SECRET` | GitHub OAuth App Client Secret             |
| `GITHUB_WEBHOOK_SECRET`| Webhook secret set when registering webhook|
| `GEMINI_API_KEY`       | Google Gemini API key                      |
| `FRONTEND_URL`         | Frontend origin URL for CORS               |

---

## 🏃 Running Locally

### Prerequisites
- Java 21+
- Node.js 18+
- PostgreSQL (or Neon account)
- GitHub OAuth App
- Gemini API key

### Backend

```bash
# Copy and fill environment file
cp .env.example .env

# Run with environment variables
export $(cat .env | xargs)
./mvnw spring-boot:run
```

Backend runs on: `http://localhost:8080`

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on: `http://localhost:5173`

### Health Check

```bash
curl http://localhost:8080/health
```

Expected:
```json
{"status":"UP","service":"pr-reviewer","timestamp":"..."}
```

---

## 🔑 GitHub OAuth App Setup

1. Go to **GitHub → Settings → Developer Settings → OAuth Apps → New OAuth App**
2. Homepage URL: `http://localhost:5173` (or your frontend URL)
3. Authorization callback URL: `http://localhost:8080/login/oauth2/code/github`
4. Copy Client ID and Secret to `.env`

---

## 🪝 Webhook Setup

1. Go to your repository → **Settings → Webhooks → Add webhook**
2. Payload URL: `https://your-backend.render.com/webhook/github`
3. Content type: `application/json`
4. Secret: set a strong random secret, add to `.GITHUB_WEBHOOK_SECRET` env var
5. Events: select **Pull requests**

---

## 🚀 Deployment

### Backend (Render)
- New Web Service → connect GitHub repo
- Build Command: `./mvnw package -DskipTests`
- Start Command: `java -jar target/pr-reviewer-0.0.1-SNAPSHOT.jar`
- Add all environment variables in Render dashboard

### Frontend (Vercel)
- Import project → set root directory to `frontend/`
- Vercel auto-detects Vite
- Set `VITE_API_BASE_URL` env var to backend URL

### Database (Neon)
- Create project at neon.tech
- Copy connection string to `DATABASE_URL`
- Flyway runs migrations automatically on startup

---

## 📋 API Endpoints

| Method | Path              | Description                  | Auth Required |
|--------|-------------------|------------------------------|---------------|
| GET    | /health           | Health check                 | No            |
| GET    | /auth/me          | Current user info            | Yes           |
| POST   | /auth/logout      | Sign out                     | Yes           |
| GET    | /repos            | List user's repositories     | Yes           |
| POST   | /repos/select     | Enable webhook on a repo     | Yes           |
| POST   | /webhook/github   | GitHub webhook receiver      | No (HMAC)     |
| GET    | /reviews          | List all reviews             | Yes           |
| GET    | /reviews/{id}     | Get review by ID             | Yes           |

---

## 🏗 Milestones

| # | Milestone                          | Status      |
|---|------------------------------------|-------------|
| 1 | Project Initialization              | ✅ Complete |
| 2 | GitHub OAuth Login                  | ✅ Complete |
| 3 | Repository Listing                  | ✅ Complete |
| 4 | Webhook Receiver                    | ✅ Complete |
| 5 | Webhook Signature Verification      | ✅ Complete |
| 6 | PR Fetching                         | ✅ Complete |
| 7 | Context Builder                     | ✅ Complete |
| 8 | Gemini Integration                  | ✅ Complete |
| 9 | Response Validation                 | ✅ Complete |
| 10| GitHub Review Comments              | ✅ Complete |
