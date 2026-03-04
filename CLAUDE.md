# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AdminPlus is a full-stack RBAC (Role-Based Access Control) management system with:
- **Backend**: Spring Boot 3.5 + JDK 21 + PostgreSQL + Redis
- **Frontend**: Vue 3.5 + Element Plus + Vite 6 + Pinia

## Build & Development Commands

### Backend (Maven)
```bash
cd backend
mvn spring-boot:run          # Run in development mode
mvn clean package -DskipTests  # Build JAR
mvn test                     # Run all tests
mvn test -Dtest=ClassName    # Run specific test class
```

### Frontend (npm)
```bash
cd frontend
npm install                  # Install dependencies
npm run dev                  # Development server at http://localhost:5173
npm run build                # Production build
npm run lint                 # Run ESLint
npm run test                 # Run Vitest tests
```

### Docker Deployment
```bash
docker-compose up -d         # Start all services (PostgreSQL, Redis, Backend, Frontend)
docker-compose down          # Stop all services
```

## Architecture

### Backend Structure
```
backend/src/main/java/com/adminplus/
├── controller/        # REST API endpoints with @PreAuthorize
├── service/           # Business logic, @Transactional
├── repository/        # JPA repositories
├── pojo/
│   ├── entity/        # JPA entities (use Lombok @Data)
│   └── dto/           # DTOs (use Java record types)
├── common/
│   ├── config/        # Spring configuration
│   ├── security/      # JWT, UserDetailsService
│   ├── exception/     # GlobalExceptionHandler, BizException
│   └── filter/        # XSS filter, rate limiting
└── utils/             # Utility classes
```

### Frontend Structure
```
frontend/src/
├── api/               # Axios API calls
├── stores/            # Pinia stores (Setup Store syntax)
├── router/            # Vue Router with dynamic route loading
├── views/             # Page components
├── layout/            # Layout components
├── components/        # Shared components
├── directives/        # Custom directives (v-auth)
├── composables/       # Vue composables
└── utils/             # Utility functions
```

## Key Patterns

### Backend Patterns
- **DTOs**: Use Java `record` types for request/response objects
- **Entities**: Use Lombok `@Data`, extend `BaseEntity` for auditing
- **Services**: Annotate with `@Transactional`, return DTOs not entities
- **Controllers**: Use `@PreAuthorize` for permission checks
- **API Response**: All responses wrapped in `ApiResponse<T>` with code, message, data
- **Caching**: Spring Cache with Redis backend

### Frontend Patterns
- **Components**: Use `<script setup>` syntax
- **State**: Pinia stores with Composition API (Setup Store)
- **Permissions**: Use `v-auth` directive or `userStore.hasPermission()`
- **Routes**: Dynamic routes loaded from backend menu API

## Permission System
- Backend: `@PreAuthorize("hasAuthority('resource:action')")`
- Frontend: `v-auth="'resource:action'"` directive
- Permission format: `{resource}:{action}` (e.g., `user:add`, `role:edit`)

## API Endpoints
- Base URL: `http://localhost:8081/api`
- Swagger UI: `http://localhost:8081/api/swagger-ui.html`
- Health Check: `http://localhost:8081/api/actuator/health`

## Default Credentials
- Username: `admin`
- Password: `admin123`

## Required Services
- PostgreSQL on port 5432
- Redis on port 6379

## Testing
- Backend: JUnit 5 + Mockito + MockMvc, H2 for test database
- Frontend: Vitest + Vue Test Utils