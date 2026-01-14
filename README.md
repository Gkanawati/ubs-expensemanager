# UBS ExpenseManager

A **full-stack expense management system** designed for corporate employee reimbursements, developed as a **technical case study** using **Spring Boot (Java 21)** for the backend and **React + TypeScript** for the frontend, fully containerized with **Docker Compose**.

### Live Demo

- **Application**: https://ubs-expensemanager.vercel.app
- **API Documentation**: https://ubs-expensemanager.onrender.com/swagger-ui/index.html

---

## Features

- **Expense Management** - Create, edit, and track expenses with multi-currency support and receipt attachments
- **Approval Workflow** - Two-level approval process (Manager → Finance) with state-based transitions
- **Budget Controls** - Category and department-level budgets with automatic validation
- **Alert System** - Automatic notifications when budgets are exceeded
- **Reporting & Analytics** - Expenses by employee, category, and department with CSV export
- **Audit Trail** - Complete change history tracking for all expenses

---

## High-Level Architecture

```
    ┌──────────────┐     HTTP      ┌──────────────────┐     JDBC      ┌──────────────┐
    │   Frontend   │ ──────────▶   │    Backend       │ ───────────▶  │   PostgreSQL │
    │ React + Vite │               │ Spring Boot API  │               │              │
    │   :3000      │               │     :8080        │               │   :5432      │
    └──────────────┘               └──────────────────┘               └──────────────┘
```

- **Frontend**: React application running with Vite (development server)
- **Backend**: Spring Boot REST API with mock authentication
- **Database**: PostgreSQL with schema versioning via Flyway
- **Infrastructure**: Docker & Docker Compose

---

## 📁 Repository Folder Structure

```
expense-manager/
│
├── backend/                     # Spring Boot backend (Java 21)
│   ├── Dockerfile               # Multi-stage build (Maven + JRE)
│   ├── pom.xml                  # Maven dependency management
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/ubs/expensemanager/
│       │   │       ├── ExpenseManagerApplication.java  # Application entry point
│       │   │       ├── config/      # Configuration classes (Security, CORS, etc.)
│       │   │       ├── controller/  # REST controllers (User, Expense, Category, etc.)
│       │   │       ├── dto/         # Data Transfer Objects
│       │   │       ├── event/       # Application events
│       │   │       ├── exception/   # Custom exceptions and handlers
│       │   │       ├── mapper/      # Entity <-> DTO mappers
│       │   │       ├── model/       # JPA entities (User, Expense, Department, etc.)
│       │   │       ├── repository/  # Spring Data JPA repositories
│       │   │       ├── security/    # JWT and authentication logic
│       │   │       └── service/     # Business logic layer
│       │   │
│       │   └── resources/
│       │       ├── application.yml  # Application configuration
│       │       └── db/migration/    # Flyway migrations
│       │
│       └── test/
│           ├── java/com/ubs/expensemanager/  # Unit and integration tests
│           └── resources/           # Test configurations and data
│
├── frontend/                   # React + TypeScript frontend
│   ├── Dockerfile               # Node.js container with Vite
│   ├── package.json             # Dependencies and scripts
│   ├── tsconfig.json            # TypeScript configuration
│   ├── vite.config.ts           # Vite configuration
│   ├── playwright.config.ts     # Playwright E2E test configuration
│   └── src/
│       ├── main.tsx              # React bootstrap
│       ├── App.tsx               # Root component
│       ├── api/                  # API client services
│       ├── components/           # Reusable UI components (DataTable, Dialogs, etc.)
│       ├── config/               # Application configuration
│       ├── hooks/                # Custom React hooks
│       ├── lib/                  # Utility libraries
│       ├── pages/                # Application pages (Dashboard, Expenses, Users, etc.)
│       ├── services/             # Business logic and API communication
│       ├── types/                # TypeScript type definitions
│       └── utils/                # Helper functions
│   └── test/
│       ├── e2e/                  # Playwright end-to-end tests
│       └── unit/                 # Vitest unit tests
│
├── docker-compose.yml           # Container orchestration
├── .gitignore                   # Git ignored files
└── README.md                    # Project documentation
```

---

## Docker & Containerization

The project runs **entirely inside Docker containers**. There is **no need to install Java, Maven, Node.js, or PostgreSQL locally**.

### Running containers

| Service  | Port | Description             |
| -------- | ---- | ----------------------- |
| frontend | 3000 | React + Vite dev server |
| backend  | 8080 | Spring Boot REST API    |
| db       | 5432 | PostgreSQL database     |

---

## How to Run the Project

### Prerequisites

- Docker
- Docker Compose

### Start the full stack

```bash
docker compose up --build
```

Once started:

- Frontend: [http://localhost:3000](http://localhost:3000)
- Backend API: [http://localhost:8080](http://localhost:8080)
- Swagger Documentation: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 🔐 Authentication & Authorization

The application implements **JWT-based authentication** with role-based access control (RBAC).

### Authentication Flow

1. **Registration**: `POST /api/auth/register` - Create a new user account
2. **Login**: `POST /api/auth/login` - Authenticate with email/password and receive JWT token
3. **Logout**: `POST /api/auth/logout` - Clear authentication cookie

The JWT token is returned in the response body and also set as an **HttpOnly cookie** for enhanced security.

### User Roles

| Role       | Description                                 | Permissions                                |
| ---------- | ------------------------------------------- | ------------------------------------------ |
| `EMPLOYEE` | Regular employee who submits expense claims | Create and view own expenses               |
| `MANAGER`  | Department manager who approves expenses    | Approve/reject expenses, view team reports |
| `FINANCE`  | Finance team member with full system access | Manage users, categories, view all reports |

### Security Features

- **JWT Token**: Stateless authentication with configurable expiration
- **Password Encryption**: BCrypt hashing for secure password storage
- **HttpOnly Cookies**: Protection against XSS attacks
- **CORS Configuration**: Controlled cross-origin access
- **Role-based Authorization**: Method-level security with `@PreAuthorize`
- **Public Endpoints**: Swagger, authentication endpoints accessible without token

### Default Users

The application creates **4 default users** automatically on startup. All users share the same password: `123456`

| Email                | Password | Role     | Description         |
|----------------------| -------- | -------- | ------------------- |
| finance_it@ubs.com   | 123456   | FINANCE  | Finance team member |
| manager_it@ubs.com   | 123456   | MANAGER  | Department manager  |
| employee_it@ubs.com  | 123456   | EMPLOYEE | Employee One        |
| employee2_it@ubs.com | 123456   | EMPLOYEE | Employee Two        |
Obs.: it also has `_hr@ubs.com`

**Quick Start:**

1. Access the frontend at [http://localhost:3000](http://localhost:3000)
2. Login with any of the default users above
3. Finance users can create additional users via the User Management page

---

## API Documentation

The project includes **interactive API documentation** powered by Swagger/OpenAPI:

- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI Spec**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

You can explore and test all available endpoints directly from the Swagger interface.

---

## CI/CD Pipeline

The project implements a **comprehensive CI/CD pipeline** using GitHub Actions that runs automatically on:

- Push to `dev` branch
- Pull requests to `dev` or `main` branches

### Pipeline Stages

**Backend (Spring Boot)**

- Setup JDK 21 (Temurin distribution)
- Maven cache optimization
- Build and run all unit & integration tests (`mvn clean verify`)

**Frontend (React + TypeScript)**

- Setup Node.js 20
- Install dependencies with npm cache
- Run ESLint for code quality
- Execute unit tests with Vitest
- Run E2E tests with Playwright (Chromium)
- Build production bundle

**Docker**

- Validate Docker image builds for both frontend and backend
- Runs only after successful backend and frontend tests

All tests must pass before code can be merged, ensuring code quality and preventing regressions.

---

## Environment Variables

Create a `.env` file in the root directory with the following variables:

| Variable                     | Description                  | Example                 |
| ---------------------------- | ---------------------------- | ----------------------- |
| `POSTGRES_DB`                | Database name                | `expense_db`            |
| `POSTGRES_USER`              | Database username            | `postgres`              |
| `POSTGRES_PASSWORD`          | Database password            | `secret`                |
| `DB_PORT`                    | Database port                | `5432`                  |
| `JWT_SECRET`                 | Secret key for JWT tokens    | `your-256-bit-secret`   |
| `CORS_ALLOWED_ORIGINS`       | Allowed CORS origins         | `http://localhost:3000` |
| `ACTUATOR_REQUIRED_USER`     | Actuator basic auth user     | `admin`                 |
| `ACTUATOR_REQUIRED_PASSWORD` | Actuator basic auth password | `admin`                 |
| `BACKEND_ENDPOINT`           | Backend API URL              | `http://localhost:8080` |

See `.env.example` for a template.

---

## Local Development

### Using Docker (Recommended)

With Docker Compose, all environment variables are pre-configured. Just run:

```bash
docker compose up --build
```

### Without Docker

If you prefer to run the services manually:

**Backend**

Prerequisites: Java 21, PostgreSQL running locally

```bash
cd backend
./mvnw spring-boot:run
```

**Frontend**

Prerequisites: Node.js 20+

```bash
cd frontend
npm install
npm run dev
```

---

## Testing

### Backend

```bash
cd backend
./mvnw test                    # Unit tests
./mvnw verify                  # All tests including integration
```

### Frontend

```bash
cd frontend
npm run test                   # Unit tests (watch mode)
npm run test:run               # Unit tests (single run)
npm run test:coverage          # With coverage report
npm run test:e2e               # Playwright E2E tests
npm run test:e2e:headed        # E2E with browser visible
```

---

## Project Status

✔ Infrastructure ready
✔ Dockerized frontend and backend
✔ Database versioning with Flyway
✔ JWT-based authentication implemented
✔ CRUD operations for users, expenses, and categories
✔ Role-based access control (Employee, Manager, Finance)
✔ Approval workflows
✔ Comprehensive test coverage (unit + E2E)
✔ CI/CD pipeline with automated testing
✔ API documentation with Swagger

---
