# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Project Overview

AI SmartConstruct Lab — an intelligent teaching/training platform with visual workflow orchestration and LLM-based quantitative scoring. Monorepo with four modules:

| Module | Stack | Purpose |
|--------|-------|---------|
| `frontend-vue` | Vue 3.5+ / TypeScript / Vite 8 | Student, teacher, admin UIs |
| `backend-core` | Spring Boot 2.7 / Java 8 / MyBatis-Plus | Business logic, auth, persistence |
| `ai-engine` | Python / FastAPI / LangGraph | LLM scoring engine, Socratic tutoring |
| `infrastructure` | Docker Compose / MySQL 8 | Qdrant vector DB, SQL schema |

## Common Commands

### Frontend (`frontend-vue/`)
```bash
npm install                     # Install dependencies (Node 20+/22+)
npm run dev                     # Dev server at http://localhost:5173
npm run build                   # Type-check + production build
npm run type-check              # vue-tsc type checking only
npx vue-tsc --build             # Same as type-check, without npm overhead
```

### Backend (`backend-core/`)
```bash
./mvnw.cmd clean install -DskipTests   # Build (Java 8 required, set JAVA_HOME)
./mvnw.cmd spring-boot:run             # Start at http://localhost:8080
./mvnw.cmd test                        # Run tests
```

### AI Engine (`ai-engine/`)
```bash
python -m venv venv && .\venv\Scripts\activate
pip install -r requirements.txt
uvicorn main:app --reload --port 8000   # Starts at http://localhost:8000
```

### Infrastructure
```bash
docker-compose -f infrastructure/docker-compose.yml up -d   # Qdrant on :6333 (REST), :6334 (gRPC)
```

## Architecture

### Backend layering
```
Controller (@RestController, /api/*)  →  Service interface (IService)  →  Service impl (extends ServiceImpl<Mapper, Entity>)
                                                                       →  MyBatis-Plus Mapper (extends BaseMapper<T>)
```
- JPA exists alongside MyBatis-Plus but is only used for `ddl-auto=update` schema generation — all data access goes through MyBatis-Plus mappers.
- JWT auth is stateless via `JwtAuthenticationFilter` (checks `Authorization: Bearer <token>` header). Public paths: `/api/auth/**`, `/api/public/**`.
- The `@OperationLog` annotation + `OperationLogAspect` provides AOP-driven audit logging.
- Database: `ai-smartconstruct-lab` on `localhost:3306` (root/123456).

### Frontend three-layer architecture (from `.cursorrules` — enforced strictly)
```
View (.vue)  →  Store (Pinia)  →  Service (Axios calls)
```
- **Views**: UI rendering and event capture only. No Axios calls, no complex data transforms.
- **Services (`services/modules/`)**: Axios requests and API contract types. No UI logic, no store mutations.
- **Stores (`stores/modules/`)**: State management via Pinia Composition API. Import services for async actions, never call Axios directly.
- **Types** must be defined in `services/types/` before writing views or services. Must align 1:1 with backend DTOs.

### Router
- `src/router/index.ts` merges route modules from `src/router/modules/` (admin, auth, student, teacher, training).
- Auth guard: non-auth pages require `auth_token` in localStorage; logged-in users hitting `/auth` are redirected to their role-based home (role 1→/admin, 2→/teacher/workbench, 3→/student/workbench).

### AI Engine
- Early scaffolding. Only `GET /api/health` is implemented.
- Intended stack: FastAPI routes → services → LangGraph agents → Qwen LLM (via Alibaba Cloud DashScope OpenAI-compatible endpoint).
- Qdrant provides vector storage for RAG; `cache_models.py` pre-downloads `all-MiniLM-L6-v2` embedding model.

### Database schema
Full DDL is in `infrastructure/db_mysql.txt` (22 tables). Hibernate `ddl-auto=update` manages schema evolution — no Flyway/Liquibase migrations.

## Code Conventions

- **Commits**: Conventional Commits (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`). See `git log` for recent examples.
- **Frontend styles**: Must use CSS variables from `assets/styles/variables.css` — absolutely NO hardcoded colors/padding/margins or inline styles. All `<style>` blocks must be `<style scoped>`.
- **TypeScript**: Strict mode, `any` is forbidden. `<script setup lang="ts">` only.
- **API base URL**: Hardcoded in `src/services/api.ts` as `http://localhost:8080`. No `.env` file exists.
- **Auth storage**: `localStorage` keys are `auth_token` and `auth_user`. No cookie strategy, no token refresh.

## Key Files

| File | Role |
|------|------|
| `backend-core/src/main/java/.../config/SecurityConfig.java` | CORS origins, public/authenticated path rules |
| `backend-core/src/main/resources/application.properties` | DB, JWT, MyBatis-Plus config |
| `frontend-vue/src/services/api.ts` | Axios instance, base URL, auth interceptor |
| `frontend-vue/src/router/index.ts` | Route merge, auth guard, role redirect |
| `frontend-vue/src/rules/.cursorrules` | Frontend architectural rules (authoritative) |
| `infrastructure/db_mysql.txt` | Complete SQL DDL (22 tables) |
