# Setup Guide

This guide explains how to run the SkillSync project locally.

## Prerequisites

Install the following tools:

- Java 21
- Maven
- Node.js and npm
- Docker Desktop
- Git
- MySQL, if running services manually without Docker

## Repository Structure

Expected clean project structure:

```text
Capgemini Sprint SkillSync/
├── backend/
│   ├── skillsync-api-gateway/
│   ├── skillsync-auth-service/
│   ├── skillsync-common/
│   ├── skillsync-config-server/
│   ├── skillsync-eureka-server/
│   ├── skillsync-user-service/
│   ├── skillsync-mentor-service/
│   ├── skillsync-skill-service/
│   ├── skillsync-session-service/
│   ├── skillsync-review-service/
│   ├── skillsync-group-service/
│   └── skillsync-notification-service/
├── frontend/
│   └── skillsync-frontend/
├── docs/
├── docker/
├── grafana/
├── docker-compose.yml
└── README.md
```

## Environment Variables

Do not push real secrets to GitHub. Create a `.env.example` file with dummy values:

```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
APP_MAIL_FROM=your-email@gmail.com
JWT_SECRET=your-jwt-secret
```

For local execution, create a real `.env` file on your machine only.

## Running With Docker

From the project root:

```bash
docker compose up --build
```

Important note: since services are placed inside `backend/`, the `docker-compose.yml` build paths should use:

```yaml
build: ./backend/skillsync-auth-service
```

For frontend:

```yaml
build: ./frontend/skillsync-frontend
```

## Manual Backend Run Order

If running services manually, start infrastructure first:

1. MySQL
2. RabbitMQ
3. Zipkin
4. Config Server
5. Eureka Server
6. Backend microservices
7. API Gateway

Recommended service startup order:

```text
skillsync-config-server
skillsync-eureka-server
skillsync-auth-service
skillsync-user-service
skillsync-mentor-service
skillsync-skill-service
skillsync-session-service
skillsync-review-service
skillsync-group-service
skillsync-notification-service
skillsync-api-gateway
```

To run a service manually:

```bash
cd backend/skillsync-auth-service
mvn spring-boot:run
```

## Running Frontend

From frontend folder:

```bash
cd frontend/skillsync-frontend
npm install
npm start
```

or:

```bash
ng serve
```

## Important URLs

Common local URLs:

```text
API Gateway: http://localhost:8080
Eureka Server: http://localhost:8761
RabbitMQ Management: http://localhost:15672
Zipkin: http://localhost:9411
Grafana: http://localhost:3000
Swagger UI: http://localhost:8080/swagger-ui.html
```

## Cleanup Before GitHub Push

Do not push generated or local folders:

```text
target/
logs/
node_modules/
dist/
coverage/
.angular/
.metadata/
.m2/
.env
```

Use `.gitignore` to ignore these files and folders.

## Basic Test Flow

1. Register a learner.
2. Login and copy JWT token.
3. Create or update user profile.
4. Apply as mentor.
5. Admin approves mentor application.
6. Learner books session with approved mentor.
7. Mentor accepts session.
8. Notification event is published and consumed.
9. Learner submits review.
10. Fetch mentor rating.

## Troubleshooting

If services cannot communicate:

- Check Eureka Server.
- Check service names.
- Check API Gateway routes.
- Check Config Server availability.
- Check Docker network.

If notification is not working:

- Check RabbitMQ is running.
- Check `session.queue`.
- Check mail environment variables.
- Check Notification Service logs.

If login fails:

- Check Auth DB.
- Check password encryption.
- Check JWT secret.
- Check role initialization.
