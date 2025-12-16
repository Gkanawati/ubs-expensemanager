# UBS ExpenseManager

A **full-stack expense management system** designed for corporate employee reimbursements, developed as a **technical case study** using **Spring Boot (Java 21)** for the backend and **React + TypeScript** for the frontend, fully containerized with **Docker Compose**.


---

##  High-Level Architecture

```
    ┌──────────────┐     HTTP      ┌──────────────────┐     JDBC      ┌──────────────┐
    │   Frontend   │ ──────────▶   │    Backend      │ ───────────▶  │   PostgreSQL │
    │ React + Vite │               │ Spring Boot API  │               │              │
    │   :5173      │               │     :8080        │               │   :5432      │
    └──────────────┘               └──────────────────┘               └──────────────┘
```

* **Frontend**: React application running with Vite (development server)
* **Backend**: Spring Boot REST API with mock authentication
* **Database**: PostgreSQL with schema versioning via Flyway
* **Infrastructure**: Docker & Docker Compose

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
│       │   │       ├── domain/      # Domain entities (future)
│       │   │       ├── repository/  # JPA repositories (future)
│       │   │       ├── service/     # Business logic (future)
│       │   │       └── controller/  # REST controllers (future)
│       │   │
│       │   └── resources/
│       │       ├── application.yml  # Application configuration
│       │       └── db/migration/    # Flyway migrations (V1__init.sql)
│       │
│       └── test/
│           └── java/com/ubs/expensemanager/
│               └── ExpenseManagerApplicationTests.java
│
├── frontend/                   # React + TypeScript frontend
│   ├── Dockerfile               # Node.js container with Vite
│   ├── package.json             # Dependencies and scripts
│   ├── tsconfig.json            # TypeScript configuration
│   ├── vite.config.ts           # Vite configuration
│   └── src/
│       ├── main.tsx              # React bootstrap
│       ├── App.tsx               # Root component
│       ├── pages/                # Application pages (login, expenses, etc.)
│       ├── components/           # Reusable UI components
│       ├── services/             # API communication layer (future)
│       └── styles/               # Global styles
│
├── docker-compose.yml           # Container orchestration
├── .gitignore                   # Git ignored files
└── README.md                    # Project documentation
```

---

##  Docker & Containerization

The project runs **entirely inside Docker containers**. There is **no need to install Java, Maven, Node.js, or PostgreSQL locally**.

### Running containers

| Service  | Port | Description             |
| -------- | ---- | ----------------------- |
| frontend | 5173 | React + Vite dev server |
| backend  | 8080 | Spring Boot REST API    |
| db       | 5432 | PostgreSQL database     |

---

## How to Run the Project

### Prerequisites

* Docker
* Docker Compose

### Start the full stack

```bash
docker compose up --build
```

Once started:

* Frontend: [http://localhost:5173](http://localhost:5173)
* Backend: [http://localhost:8080](http://localhost:8080)

---

## Authentication (Current State)

The backend uses **Spring Security default configuration (development mode)**.

* A default user is automatically generated on startup.
* The generated password is printed in the backend logs:

```text
Using generated security password: <password>
```

### Temporary credentials

| Field    | Value                |
| -------- | -------------------- |
| Username | `user`               |
| Password | generated at startup |

This authentication mechanism is **temporary** and will be replaced by fixed mock users and later by JWT-based authentication.

---

## Project Status

✔ Infrastructure ready
✔ Dockerized frontend and backend
✔ Database versioning with Flyway
✔ Backend starts correctly
✔ Frontend starts correctly
❌ Frontend ↔ Backend integration (in progress)
❌ Business logic implementation (in progress)

---

##  Planned Next Steps

* Introduce fixed mock users (employee / manager / finance)
* Implement API-based authentication (JWT)
* Create CRUD for employees and expenses
* Implement approval workflows
* Add reporting and dashboards
* Enable CI/CD with merge validation

---
