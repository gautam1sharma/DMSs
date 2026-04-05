# Serene DMS

A full-stack **Dealer Management System** for brand **Serene**.

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.3.5 (Java 21) |
| Frontend | React 18 + Vite + TypeScript |
| Database | MySQL 8 on port **3310** |
| Security | JWT + BCrypt + Spring Security |
| Docs | Swagger UI at `/swagger-ui.html` |

## Quick Start

### Prerequisites
- Java 21
- Maven 3.8+
- Node.js 18+
- MySQL running on port 3310

### 1. Start the Backend
```bash
cd backend
mvn spring-boot:run
```

The backend starts on **http://localhost:8080**

### 2. Start the Frontend
```bash
cd frontend
npm run dev
```

The frontend starts on **http://localhost:5173**

## Default Credentials

| Role  | Email | Password |
|---|---|---|
| Admin | admin@serene.com | Admin@123 |

## Key URLs

| URL | Description |
|---|---|
| http://localhost:5173 | Frontend app |
| http://localhost:8080/swagger-ui.html | API docs |
| http://localhost:8080/v3/api-docs | OpenAPI JSON |
| http://localhost:8080/actuator/health | Health check |

## Roles
- **ADMIN** — Full system access, manages dealers and users
- **DEALER** — Manages own customers, vehicles, orders and inquiries
- **CUSTOMER** — Portal access (view own orders/inquiries)

## Features
- JWT authentication with account locking (5 failed attempts)
- Multi-dealer support — each dealer independently manages their customers
- Direct dealer → customer relationship (no employee layer)
- Server-side pagination and sorting
- Flyway database migrations
- Swagger/OpenAPI documentation
- Dark glassmorphism premium UI
