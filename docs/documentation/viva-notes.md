# Viva Notes

This document contains short answers and explanation points for SkillSync viva or demo.

## Project Introduction

SkillSync is a microservices-based learning and mentorship platform. It connects learners with mentors and allows learners to book sessions, join groups, submit reviews, and receive notifications. Admin manages skills and mentor approval.

## Main Roles

## Learner

- Register and login
- Manage profile
- View skills
- Search mentors
- Apply as mentor
- Book sessions
- Join groups
- Submit reviews
- View notifications

## Mentor

- Login
- Manage mentor profile
- View session requests
- Accept or reject sessions
- View reviews
- Receive notifications

## Admin

- Login
- Manage skills
- View users
- Approve mentor applications
- Reject mentor applications
- View system data

## Why Microservices?

Microservices divide the project into smaller independent services. Each service has a specific responsibility. This improves maintainability, scalability, and fault isolation.

Example:

- Auth Service handles login and JWT.
- User Service handles profile.
- Session Service handles booking.
- Review Service handles reviews.
- Notification Service handles notifications.

## What Is API Gateway?

API Gateway is the single entry point for all client requests. It validates JWT tokens, routes requests to the correct microservice, and forwards user id and role to downstream services.

## What Is Eureka Server?

Eureka Server is used for service discovery. All microservices register themselves with Eureka, and other services can find them by service name.

## What Is Config Server?

Config Server provides centralized configuration. Instead of writing repeated configuration in every service, services can fetch configuration from Config Server.

## What Is JWT?

JWT stands for JSON Web Token. In SkillSync, after login, Auth Service generates a JWT token. The client sends this token with every protected request. API Gateway validates the token and extracts user id and role.

## Why PasswordEncoder?

Passwords should not be stored as plain text. `PasswordEncoder` encrypts the password before saving it in the database. During login, the entered password is matched with the encrypted password.

## What Is Feign Client?

Feign Client is used for synchronous communication between microservices.

Examples:

- Auth Service calls User Service during registration.
- Mentor Service calls Auth Service during mentor approval.
- Session Service calls User Service and Mentor Service.
- Review Service calls Session Service.
- Notification Service calls User Service.

## What Is RabbitMQ?

RabbitMQ is used for asynchronous communication. In SkillSync, Session Service publishes session events to RabbitMQ, and Notification Service consumes those events to send email notifications.

## Why RabbitMQ In This Project?

Notification sending should not block session booking. So Session Service only publishes an event, and Notification Service handles email separately. This makes the system loosely coupled.

## What Is ResponseDTO?

`ResponseDTO` is a common response wrapper used across services. It usually contains:

- `success`
- `message`
- `data`

This keeps API responses consistent.

## Important Feature Flow: Registration And Login

1. User sends registration request.
2. Auth Service checks duplicate email.
3. Password is encrypted.
4. User and role are saved in Auth DB.
5. Auth Service calls User Service to create profile.
6. During login, Auth Service validates credentials.
7. JWT token is generated and returned.

## Important Feature Flow: Mentor Approval

1. Learner applies as mentor.
2. Mentor profile is stored with `PENDING` status.
3. Admin approves the application.
4. Mentor status becomes `APPROVED`.
5. Mentor Service calls Auth Service.
6. Auth Service updates user role to `ROLE_MENTOR`.

## Important Feature Flow: Session Booking

1. Learner sends session booking request.
2. Session Service validates learner.
3. Session Service validates mentor.
4. Session is saved with `REQUESTED` status.
5. Session Service publishes `SESSION_BOOKED` event to RabbitMQ.
6. Notification Service consumes event and sends email to mentor.

## Important Feature Flow: Review And Rating

1. Learner submits review.
2. Review Service checks learner role.
3. Review Service calls Session Service to validate session.
4. It checks that the session belongs to the learner.
5. It prevents duplicate review for same session.
6. Review is saved.
7. Average rating is calculated using review data.

## Diagram Explanation Points

## Use Case Diagram

The Use Case Diagram shows how Learner, Mentor, and Admin interact with SkillSync. Learner can book sessions and submit reviews. Mentor can manage sessions. Admin can manage skills and approve mentors.

## Architecture Diagram

The Architecture Diagram shows API Gateway, microservices, Eureka Server, Config Server, RabbitMQ, databases, and monitoring tools. It explains how requests and events move through the system.

## ER Diagram

The ER Diagram shows database entities like AuthUser, Role, UserProfile, Mentor, Session, Review, Group, and GroupMember. It shows how users, sessions, reviews, and groups are related.

## DFD Level 0

DFD Level 0 shows SkillSync as one complete system interacting with Learner, Mentor, and Admin.

## DFD Level 1

DFD Level 1 breaks the system into internal processes like Authentication, User Profile Management, Mentor Management, Session Booking, Review and Rating, Group Management, and Notification Management.

## Class Diagram

The Class Diagram shows controller, service, repository, entity, Feign client, and utility classes service-wise. It explains the layered backend structure.

## Common Viva Questions

### Why did you use API Gateway?

To provide a single entry point, route requests, validate JWT, and forward user context to services.

### Why did you separate Auth Service and User Service?

Auth Service handles login credentials and security, while User Service handles profile data. This follows separation of responsibility.

### How is role-based access handled?

JWT contains user id and role. API Gateway validates the token and forwards role in `X-User-Role`. Services check this role before allowing operations.

### How does notification work?

Session Service publishes session events to RabbitMQ. Notification Service listens to the queue and sends email notifications.

### How do you prevent duplicate reviews?

Review Service checks if a review already exists for the same user and session using repository methods.

### What happens when admin approves a mentor?

Mentor status changes to `APPROVED`, and Mentor Service calls Auth Service to update the user's role to `ROLE_MENTOR`.

## Closing Summary

SkillSync is a modular mentorship platform built using Spring Boot microservices. It includes secure authentication, service discovery, centralized configuration, inter-service communication, session booking, review management, RabbitMQ notifications, and monitoring support.
