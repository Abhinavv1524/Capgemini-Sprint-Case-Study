```
  ███████╗██╗  ██╗██╗██╗     ██╗     ███████╗██╗   ██╗███╗   ██╗ ██████╗
  ██╔════╝██║ ██╔╝██║██║     ██║     ██╔════╝╚██╗ ██╔╝████╗  ██║██╔════╝
  ███████╗█████╔╝ ██║██║     ██║     ███████╗ ╚████╔╝ ██╔██╗ ██║██║
  ╚════██║██╔═██╗ ██║██║     ██║     ╚════██║  ╚██╔╝  ██║╚██╗██║██║
  ███████║██║  ██╗██║███████╗███████╗███████║   ██║   ██║ ╚████║╚██████╗
  ╚══════╝╚═╝  ╚═╝╚═╝╚══════╝╚══════╝╚══════╝   ╚═╝   ╚═╝  ╚═══╝ ╚═════╝
```

# SkillSync

### Peer Learning & Mentor Matching Platform

[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=flat-square&logo=spring)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-17+-red?style=flat-square&logo=angular)](https://angular.io/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)](https://www.mysql.com/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.x-FF6600?style=flat-square&logo=rabbitmq)](https://www.rabbitmq.com/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)](https://www.docker.com/)

> A full-stack mentorship platform where learners connect with mentors, book sessions, join study groups, and grow together — built with Angular and a Spring Boot microservices backend.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [User Roles](#user-roles)
- [Architecture](#architecture)
- [Microservices](#microservices)
- [Frontend](#frontend)
- [Database Design](#database-design)
- [Authentication & Authorization](#authentication--authorization)
- [Event-Driven Notifications](#event-driven-notifications)
- [Technology Stack](#technology-stack)
- [Setup Guide](#setup-guide)
- [Key API Endpoints](#key-api-endpoints)
- [Important URLs](#important-urls)
- [Repository Structure](#repository-structure)
- [Documentation](#documentation)
- [Demo Flow](#demo-flow)
- [Author](#author)

---

## Overview

SkillSync is designed around a simple idea: learning is better when you have the right mentor. The platform lets learners search for mentors by skill, request one-on-one sessions, join peer groups, and leave reviews — while mentors get a clean interface to manage their availability and incoming requests.

Behind the scenes, the system is split into independent microservices. Each service is responsible for exactly one domain. They talk to each other using Feign clients for real-time calls and RabbitMQ for background events like email notifications. An API Gateway sits in front of everything, handling JWT validation and routing before a request ever reaches a service.

**What this project demonstrates:**

- Microservices architecture with Spring Boot and Spring Cloud
- JWT-secured API Gateway with role forwarding
- Asynchronous event-driven communication via RabbitMQ
- Database-per-service design with MySQL
- Full CRUD flows with role-based access control
- Angular frontend with role-specific dashboards
- Observability with Zipkin, Grafana, and Loki

---

## Features

| Feature                     | What it does                                                                                                        |
| --------------------------- | ------------------------------------------------------------------------------------------------------------------- |
| JWT Authentication          | Stateless token-based login. Every request carries a signed token verified at the gateway.                          |
| Role-Based Access Control   | Three roles — Learner, Mentor, Admin — each with protected routes and service-level enforcement.                    |
| Mentor Application Workflow | Learners apply to become mentors. Admin reviews and approves or rejects. Role is updated automatically on approval. |
| Session Booking             | Full lifecycle: Learner requests → Mentor accepts/rejects → Session completed or cancelled.                         |
| Async Email Notifications   | RabbitMQ decouples session events from email sending. Notifications go out without blocking the booking flow.       |
| Reviews & Ratings           | Learners review mentors after sessions. Duplicate reviews are prevented. Average rating is computed per mentor.     |
| Learning Groups             | Users can create groups, join others, and leave whenever they want.                                                 |
| Service Discovery           | Eureka lets services find each other by name instead of hardcoded URLs.                                             |
| Centralized Config          | One Config Server distributes shared properties to all services at startup.                                         |
| Distributed Tracing         | Zipkin traces requests across services. Grafana + Loki handle logs.                                                 |

---

## User Roles

### Learner

A regular user who wants to learn. After registering, they get the `LEARNER` role by default.

**Can:** Register, log in, manage their profile, browse skills, book sessions with mentors, join learning groups, and submit reviews after a completed session.

### Mentor

A user who has been approved by an admin to offer mentorship. They start as a Learner and apply through the platform.

**Can:** Manage their mentor profile, view incoming session requests, accept or reject sessions, and see their reviews and average rating.

### Admin

The platform administrator. There is no public registration for admins — they are assigned directly.

**Can:** Create and manage skills, view all users, and approve or reject mentor applications.

---

## Architecture

### Request Flow

Every request from the frontend passes through the API Gateway. The gateway validates the JWT, extracts the user ID and role, and forwards them as headers to the target microservice. Services never validate tokens themselves — they trust the headers forwarded by the gateway.

```
Frontend (Angular :4200)
        |
        |  HTTP + Authorization: Bearer <token>
        v
API Gateway (:8080)
        |
        |  Validates JWT
        |  Extracts X-User-Id, X-User-Role
        |  Routes to correct service
        v
 ┌──────┬──────┬────────┬─────────┬──────────┬─────────┐
 │ Auth │ User │ Mentor │ Session │  Review  │  Group  │
 │:8081 │:8082 │ :8083  │  :8084  │  :8085   │  :8086  │
 └──────┴──────┴────────┴─────────┴──────────┴─────────┘
        |
        |  All services registered on Eureka (:8761)
        |  All services pull config from Config Server (:8888)
```

### Inter-Service Communication

There are two ways services communicate:

**Synchronous (Feign Client)** — used when a service needs data from another service immediately to complete a request. For example, the Session Service calls the Mentor Service to validate that a mentor exists before booking a session.

**Asynchronous (RabbitMQ)** — used when a service needs to trigger a background action without waiting for it. Session state changes publish events to a RabbitMQ queue, and the Notification Service picks them up independently.

```
Auth Service      --Feign-->  User Service          (create profile on register)
Session Service   --Feign-->  User Service           (validate learner)
Session Service   --Feign-->  Mentor Service         (validate mentor)
Notification Svc  --Feign-->  User Service           (fetch email address)

Session Service   --RabbitMQ-->  Notification Service   (session events)
```

---

## Microservices

### Config Server

Runs first. Holds shared configuration (database URLs, RabbitMQ settings, JWT secret, mail config) in one place. Every other service fetches its config from here at startup instead of maintaining its own `application.properties` for shared values.

### Eureka Server

Service registry. Every microservice registers itself here on startup with its name and address. When Service A needs to call Service B, it asks Eureka for B's current address — no hardcoded URLs needed. This also makes it easy to run multiple instances of a service for load balancing.

### API Gateway

The only entry point for external traffic. Responsibilities:

- Validate the JWT on every protected request
- Extract `X-User-Id` and `X-User-Role` from the token and forward them as headers
- Allow unauthenticated access only to `/api/auth/**`
- Block everything else if no valid token is present

### Auth Service

Handles everything related to user identity and credentials.

Registration flow:

1. Checks for duplicate email
2. Encrypts password with BCrypt
3. Assigns `LEARNER` role
4. Saves user in Auth DB
5. Calls User Service via Feign to create the profile

Login flow:

1. Validates email and password
2. Generates a signed JWT containing user ID and role
3. Returns token to client

Also exposes an internal endpoint called by the Mentor Service to upgrade a user's role from `LEARNER` to `MENTOR` after admin approval.

### User Service

Stores and manages user profile data — name, bio, email, skills list. Separate from Auth Service so that profile data and credentials are independently managed. Profile is created automatically during registration via a Feign call from Auth Service.

### Mentor Service

Manages the mentor application and approval lifecycle.

```
Learner submits application  -->  Status: PENDING
Admin approves               -->  Status: APPROVED  -->  Auth Service updates role to MENTOR
Admin rejects                -->  Status: REJECTED
```

Once approved, the user can be booked as a mentor and their profile shows up in search results.

### Skill Service

Admin-managed catalogue of skills on the platform (e.g. Java, Python, UI/UX Design). Skills are used to tag mentor profiles and help learners find the right match. Duplicate skill names are prevented at the service level.

### Session Service

The core transactional service. Manages the entire session lifecycle.

```
REQUESTED  -->  ACCEPTED   -->  COMPLETED
           -->  REJECTED
           -->  CANCELLED
```

Before booking, the service validates the learner (via User Service) and the mentor (via Mentor Service). After every state change, an event is published to RabbitMQ so that notifications can be sent asynchronously.

### Review Service

Learners can leave a review and star rating after a session. Before saving, the service checks that a completed session between the learner and mentor actually exists, and that the learner has not already reviewed this mentor. Average rating per mentor is calculated on demand.

### Group Service

Allows users to create named learning groups, join existing ones, and leave at any time. Groups store a list of members. Any user can create a group; any user can join one.

### Notification Service

A pure consumer — it does not expose any HTTP endpoints. It listens on a RabbitMQ queue for session events, fetches the relevant user's email via the User Service, and sends an email notification.

Handled events:

- `SESSION_BOOKED` — notifies the mentor that a learner has requested a session
- `SESSION_ACCEPTED` — notifies the learner that their session has been accepted

This service is completely decoupled from the Session Service. If the Notification Service is down, RabbitMQ holds the events and delivers them when it comes back up — no notifications are lost.

### Common Module

A shared library (not a deployable service) that contains reusable classes used across services: `ResponseDTO`, `BadRequestException`, `ResourceNotFoundException`, `UnauthorizedException`. Included as a Maven dependency in other services.

---

## Frontend

The Angular frontend provides role-specific dashboards for each user type. It stores the JWT in localStorage and attaches it as a Bearer token on every API call. The API Gateway at `http://localhost:8080` is the only backend address the frontend talks to.

**Learner dashboard:** Profile management, mentor browse, session booking, group directory, review submission.

**Mentor dashboard:** Incoming session requests, accept/reject controls, profile editing, review summary.

**Admin dashboard:** Skill creation and management, pending mentor applications, user overview.

```bash
# Install dependencies
cd frontend/skillsync-frontend
npm install

# Start development server — serves at http://localhost:4200
npm start

# Build for production
npm run build
```

---

## Database Design

Each microservice has its own isolated MySQL database. No service queries another service's database directly — if it needs data from another domain, it makes an API call. This is the **database-per-service** pattern, which keeps services independently deployable and prevents tight coupling at the data layer.

**Core entities and relationships:**

```
AuthUser          stores credentials, has a Role
UserProfile       stores public profile data, belongs to AuthUser
Mentor            extends UserProfile, has application Status
Skill             platform-level tag managed by Admin
Session           links a Learner (UserProfile) and a Mentor, has Status
Review            written by a Learner, targets a Mentor, linked to a Session
Group             named group with a creator
GroupMember       junction table linking UserProfile to Group
```

**Key relationships:**

- `AuthUser` to `UserProfile` — one-to-one, created together on registration
- `UserProfile` to `Mentor` — one-to-one, created on mentor application
- `Session` links a `UserProfile` (learner) and a `Mentor`
- `Review` is tied to a specific `Session` — one review allowed per session
- `GroupMember` is a many-to-many join between `UserProfile` and `Group`

Full ERD: `docs/diagrams/ER Diagram.png`
Detailed write-up: `docs/documentation/database-design.md`

---

## Authentication & Authorization

### How JWT Works in This System

When a user logs in, Auth Service generates a JWT containing:

- `sub` — the user's internal ID
- `role` — their current role (`LEARNER`, `MENTOR`, or `ADMIN`)
- `iat` / `exp` — issued-at and expiry timestamps

The client stores this token and sends it with every request:

```
Authorization: Bearer <token>
```

The API Gateway intercepts the request, validates the token's signature and expiry, and extracts the payload. It then forwards two custom headers downstream:

```
X-User-Id:   <extracted user ID>
X-User-Role: <extracted role>
```

Microservices never touch the JWT themselves. They read `X-User-Id` and `X-User-Role` from the forwarded headers and apply their own role checks before executing business logic.

### Role Enforcement Examples

| Action                     | Required Role | Enforced In     |
| -------------------------- | ------------- | --------------- |
| Create a skill             | ADMIN         | Skill Service   |
| Approve mentor application | ADMIN         | Mentor Service  |
| Book a session             | LEARNER       | Session Service |
| Accept or reject a session | MENTOR        | Session Service |
| Submit a review            | LEARNER       | Review Service  |
| View all users             | ADMIN         | User Service    |

---

## Event-Driven Notifications

### Why RabbitMQ?

Session booking should respond instantly to the user. Sending an email is a slow, external operation — if it fails or takes a few seconds, the booking response would be delayed too. RabbitMQ solves this by decoupling the two: the Session Service publishes an event and moves on; the Notification Service picks it up and sends the email in the background.

If the Notification Service is temporarily down, RabbitMQ holds the message in the queue and delivers it when the service restarts. No events are lost.

### Flow: Session Booked

```
1.  Learner calls POST /api/sessions
2.  Session Service validates learner and mentor via Feign
3.  Session saved to DB with status REQUESTED
4.  Session Service publishes SESSION_BOOKED event to RabbitMQ
5.  Response returned to learner immediately
--- background ---
6.  Notification Service consumes SESSION_BOOKED from queue
7.  Notification Service calls User Service to get mentor's email
8.  Email sent to mentor: "You have a new session request"
```

### Flow: Session Accepted

```
1.  Mentor calls PUT /api/sessions/{id}/accept
2.  Session status updated to ACCEPTED in DB
3.  Session Service publishes SESSION_ACCEPTED event to RabbitMQ
4.  Response returned to mentor immediately
--- background ---
5.  Notification Service consumes SESSION_ACCEPTED from queue
6.  Notification Service calls User Service to get learner's email
7.  Email sent to learner: "Your session has been accepted"
```

---

## Technology Stack

### Backend

| Technology           | Version | Purpose                                         |
| -------------------- | ------- | ----------------------------------------------- |
| Java                 | 21      | Primary backend language                        |
| Spring Boot          | 3.x     | Microservice framework                          |
| Spring Cloud Gateway | 4.x     | API Gateway, JWT filter, routing                |
| Spring Cloud Eureka  | 4.x     | Service registry and discovery                  |
| Spring Cloud Config  | 4.x     | Centralized external configuration              |
| Spring Security      | 6.x     | Security layer and filter chain                 |
| JWT (jjwt)           | 0.11+   | Token generation and validation                 |
| Spring Data JPA      | 3.x     | ORM and database access layer                   |
| OpenFeign            | 4.x     | Declarative HTTP client for inter-service calls |
| RabbitMQ             | 3.x     | Message broker for async events                 |
| MySQL                | 8.0     | Relational database (one per service)           |
| Docker + Compose     | —       | Containerization and local orchestration        |
| Zipkin               | 2.x     | Distributed request tracing                     |
| Grafana + Loki       | —       | Log aggregation and dashboards                  |
| Maven                | 3.8+    | Build tool and dependency management            |

### Frontend

| Technology       | Version | Purpose                 |
| ---------------- | ------- | ----------------------- |
| Angular          | 17+     | SPA framework           |
| Angular Material | 17+     | UI component library    |
| TypeScript       | 5.x     | Typed JavaScript        |
| RxJS             | 7.x     | Reactive async handling |
| npm              | 9+      | Package management      |

---

## Setup Guide

### Prerequisites

Make sure the following are installed before starting:

- Java 21
- Maven 3.8+
- Node.js 18+ and npm
- Docker Desktop (for Docker Compose option)
- MySQL 8.0 (for manual option)
- RabbitMQ 3.x (for manual option)

### Option 1 — Docker Compose (Recommended)

The easiest way to get everything running. Docker Compose starts all infrastructure (MySQL, RabbitMQ, Zipkin, Grafana, Loki) and all services together.

```bash
# Clone the repository
git clone <repository-url>
cd skillsync

# Start everything
docker-compose up --build
```

> **Note on build paths:** If running from this repository, update the build context paths in `docker-compose.yml` to match the current directory structure:
>
> ```yaml
> # Backend service example
> build: ./backend/skillsync-auth-service
>
> # Frontend example
> build: ./frontend/skillsync-frontend
> ```

### Option 2 — Manual Startup

Start services in this exact order. Wait for each service to fully start before running the next one.

```bash
# 1. Config Server — must be first, all services need it to start
cd backend/skillsync-config-server && mvn spring-boot:run

# 2. Eureka Server — must be second, all services register here
cd backend/skillsync-eureka-server && mvn spring-boot:run

# 3. Auth Service
cd backend/skillsync-auth-service && mvn spring-boot:run

# 4. User Service
cd backend/skillsync-user-service && mvn spring-boot:run

# 5. Mentor Service
cd backend/skillsync-mentor-service && mvn spring-boot:run

# 6. Skill Service
cd backend/skillsync-skill-service && mvn spring-boot:run

# 7. Session Service
cd backend/skillsync-session-service && mvn spring-boot:run

# 8. Review Service
cd backend/skillsync-review-service && mvn spring-boot:run

# 9. Group Service
cd backend/skillsync-group-service && mvn spring-boot:run

# 10. Notification Service
cd backend/skillsync-notification-service && mvn spring-boot:run

# 11. API Gateway — always last, needs all services on Eureka first
cd backend/skillsync-api-gateway && mvn spring-boot:run

# 12. Frontend
cd frontend/skillsync-frontend && npm install && npm start
```

> **Why this order?**
> Config Server must run first because every other service fetches its configuration from it on startup.
> Eureka must run second because all services register themselves there.
> The API Gateway must run last because it needs to discover all services on Eureka before it can route requests.

### Environment Variables

Copy `.env.example` to `.env` and fill in your values:

```bash
cp .env.example .env
```

```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
APP_MAIL_FROM=your-email@gmail.com
JWT_SECRET=your-jwt-secret-key-minimum-32-characters
```

> For Gmail, `MAIL_PASSWORD` should be an **App Password**, not your regular account password. Generate one at: Google Account → Security → 2-Step Verification → App Passwords.

---

## Key API Endpoints

All requests go through the API Gateway at `http://localhost:8080`. Protected endpoints require `Authorization: Bearer <token>` in the request header.

### Authentication (Public)

```http
POST   /api/auth/register       Register a new user account
POST   /api/auth/login          Login and receive a JWT token
```

### User Profile

```http
GET    /api/users/me            Get your own profile
PUT    /api/users/me            Update your profile
GET    /api/users/{id}          Get a user profile by ID
```

### Mentor

```http
POST   /api/mentors/apply            Apply as a mentor (Learner only)
GET    /api/mentors                  Get all approved mentors
GET    /api/mentors/{id}             Get a mentor profile by ID
PUT    /api/mentors/profile          Update mentor profile (Mentor only)
GET    /api/mentors/pending          Get pending applications (Admin only)
PUT    /api/mentors/{id}/approve     Approve a mentor application (Admin only)
PUT    /api/mentors/{id}/reject      Reject a mentor application (Admin only)
```

### Sessions

```http
POST   /api/sessions                 Book a session (Learner only)
GET    /api/sessions                 Get your sessions (current user)
GET    /api/sessions/{id}            Get a session by ID
PUT    /api/sessions/{id}/accept     Accept a session (Mentor only)
PUT    /api/sessions/{id}/reject     Reject a session (Mentor only)
PUT    /api/sessions/{id}/cancel     Cancel a session (Learner only)
```

### Reviews

```http
POST   /api/reviews                              Submit a review (Learner only)
GET    /api/reviews/mentor/{mentorId}            Get all reviews for a mentor
GET    /api/reviews/mentor/{mentorId}/rating     Get average rating for a mentor
```

### Skills (Admin)

```http
POST   /api/skills          Create a new skill (Admin only)
GET    /api/skills           Get all platform skills
GET    /api/skills/{id}      Get a skill by ID
```

### Groups

```http
POST   /api/groups             Create a new group
GET    /api/groups             Get all groups
GET    /api/groups/{id}        Get a group by ID
POST   /api/groups/{id}/join   Join a group
DELETE /api/groups/{id}/leave  Leave a group
```

---

## Important URLs

| Service             | URL                                   | Notes                          |
| ------------------- | ------------------------------------- | ------------------------------ |
| Frontend            | http://localhost:4200                 | Angular application            |
| API Gateway         | http://localhost:8080                 | All API requests go here       |
| Swagger UI          | http://localhost:8080/swagger-ui.html | Interactive API docs           |
| Eureka Dashboard    | http://localhost:8761                 | View all registered services   |
| RabbitMQ Management | http://localhost:15672                | Monitor queues and messages    |
| Zipkin              | http://localhost:9411                 | Trace requests across services |
| Grafana             | http://localhost:3000                 | Logs and monitoring dashboards |

---

## Repository Structure

```
skillsync/
├── backend/
│   ├── skillsync-api-gateway/           API Gateway — JWT filter and routing rules
│   ├── skillsync-auth-service/          Registration, login, JWT generation
│   ├── skillsync-common/                Shared DTOs and exception classes
│   ├── skillsync-config-server/         Centralized configuration server
│   ├── skillsync-eureka-server/         Service discovery registry
│   ├── skillsync-user-service/          User profile management
│   ├── skillsync-mentor-service/        Mentor applications and admin approval
│   ├── skillsync-skill-service/         Platform skill catalogue
│   ├── skillsync-session-service/       Session booking and lifecycle management
│   ├── skillsync-review-service/        Reviews and star ratings
│   ├── skillsync-group-service/         Learning groups
│   └── skillsync-notification-service/  RabbitMQ consumer and email sender
├── frontend/
│   └── skillsync-frontend/              Angular SPA (all three role dashboards)
├── docs/
│   ├── diagrams/
│   │   ├── Architecture.png
│   │   ├── ER Diagram.png
│   │   ├── Use Case diagram.png
│   │   ├── Class Diagram.png
│   │   ├── DFD Level 0.png
│   │   └── DFD Level 1.png
│   ├── documentation/
│   │   ├── project-overview.md
│   │   ├── modules-and-services.md
│   │   ├── database-design.md
│   │   ├── api-summary.md
│   │   ├── setup-guide.md
│   │   └── viva-notes.md
│   └── demo/
│       └── demo-script.md
├── docker/
├── grafana/
├── docker-compose.yml
├── loki-config.yml
├── promtail-config.yml
├── .env.example
└── README.md
```

---

## Documentation

| Document           | Path                                         | What it covers                                |
| ------------------ | -------------------------------------------- | --------------------------------------------- |
| Project Overview   | `docs/documentation/project-overview.md`     | Problem statement, goals, scope               |
| Modules & Services | `docs/documentation/modules-and-services.md` | Deep-dive into each microservice              |
| Database Design    | `docs/documentation/database-design.md`      | ERD explanation and entity relationships      |
| API Summary        | `docs/documentation/api-summary.md`          | All endpoints with request/response examples  |
| Setup Guide        | `docs/documentation/setup-guide.md`          | Full local development setup instructions     |
| Viva Notes         | `docs/documentation/viva-notes.md`           | Explanation points for viva and presentations |
| Demo Script        | `docs/demo/demo-script.md`                   | Step-by-step demo walkthrough                 |

---

## Demo Flow

Recommended walkthrough order for demos or viva:

1. **Introduction** — explain the problem, the three user roles, and why microservices fit this use case
2. **Architecture diagram** — walk through the request flow from frontend to gateway to service
3. **Use Case diagram** — show what each role can and cannot do
4. **ER diagram** — explain the entities and their relationships
5. **DFD Level 0 and Level 1** — show data flow at system level and service level
6. **Class diagram** — walk through key classes and service interactions
7. **Register and Login** — create a learner account, show the JWT returned
8. **Mentor application and approval** — apply as mentor, switch to admin, approve, confirm role updated
9. **Session booking** — learner books a session with the approved mentor
10. **Notification flow** — show RabbitMQ event published and email triggered
11. **Session acceptance** — mentor accepts, learner gets notified
12. **Review and rating** — learner submits a review, show average rating endpoint response
13. **Monitoring** — show Eureka dashboard with all services registered, a Zipkin trace, and Grafana logs

---

## Author

**Abhinav Choudhary**

Built as a Capgemini Sprint project demonstrating full-stack microservices development with Java Spring Boot and Angular.

This project covers JWT authentication, role-based access control, microservices communication with Feign and RabbitMQ, database-per-service design, event-driven architecture, service discovery with Eureka, centralized configuration, and observability with Zipkin and Grafana.

---

_For questions, issues, or contributions — raise an issue or submit a pull request._
