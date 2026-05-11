# Modules And Services

This document explains the major backend services used in SkillSync.

## API Gateway

The API Gateway is the single entry point for frontend or Postman requests. It validates JWT tokens for protected APIs and routes requests to the correct microservice.

Responsibilities:

- Route API requests.
- Validate JWT tokens.
- Extract user id and role from token.
- Forward `X-User-Id` and `X-User-Role` headers to downstream services.
- Provide centralized Swagger routing.

Example routes:

- `/api/auth/**` to Auth Service
- `/api/users/**` to User Service
- `/api/mentors/**` to Mentor Service
- `/api/sessions/**` to Session Service
- `/api/reviews/**` to Review Service

## Auth Service

Auth Service handles authentication and role management.

Responsibilities:

- User registration
- User login
- Password encryption
- JWT generation
- Role assignment
- Promotion of learner to mentor after admin approval
- Sync user profile with User Service during registration

Important classes:

- `AuthController`
- `AuthService`
- `AuthServiceImpl`
- `JwtUtil`
- `UserRepository`
- `RoleRepository`
- `UserClient`

## User Service

User Service stores user profile details separately from authentication credentials.

Responsibilities:

- Create user profile
- Get user by id
- Get user by auth user id
- Update user profile
- Store user profile details such as name, email, bio, skills, and role

Important classes:

- `UserController`
- `UserService`
- `UserServiceImpl`
- `UserRepository`
- `User`
- `SecurityUtil`

## Mentor Service

Mentor Service manages mentor applications and mentor profiles.

Responsibilities:

- Learner applies as mentor
- Admin approves mentor application
- Admin rejects mentor application
- Get mentor by id
- Get mentor by user id
- Get all mentors
- Update mentor profile
- Call Auth Service to promote approved user to mentor

Important classes:

- `MentorController`
- `MentorService`
- `MentorServiceImpl`
- `MentorRepository`
- `Mentor`
- `AuthClient`

## Skill Service

Skill Service manages skill data.

Responsibilities:

- Admin creates skills
- Get all skills
- Get skill by id
- Avoid duplicate skills

Important classes:

- `SkillController`
- `SkillService`
- `SkillServiceImpl`
- `SkillRepository`
- `Skill`

## Session Service

Session Service handles the complete session booking flow.

Responsibilities:

- Learner books session with mentor
- Mentor accepts session
- Mentor rejects session
- Learner cancels session
- Get sessions by current user
- Get session by id
- Validate learner using User Service
- Validate mentor using Mentor Service
- Publish session events to RabbitMQ

Important classes:

- `SessionController`
- `SessionService`
- `SessionServiceImpl`
- `SessionRepository`
- `UserClient`
- `MentorClient`
- `SessionEventPublisher`
- `SessionEvent`

## Review Service

Review Service manages mentor reviews and ratings.

Responsibilities:

- Add review
- Get reviews by mentor
- Get average rating by mentor
- Validate session using Session Service before saving review
- Prevent duplicate review for the same session

Important classes:

- `ReviewController`
- `ReviewService`
- `ReviewServiceImpl`
- `ReviewRepository`
- `SessionClient`
- `Review`

## Group Service

Group Service manages learning groups.

Responsibilities:

- Create group
- Join group
- Leave group
- Get all groups
- Get group by id
- Store group members

Important classes:

- `GroupController`
- `GroupService`
- `GroupServiceImpl`
- `GroupRepository`
- `GroupMemberRepository`
- `Group`
- `GroupMember`

## Notification Service

Notification Service handles session-related notifications.

Responsibilities:

- Listen to RabbitMQ session events
- Process `SESSION_BOOKED` and `SESSION_ACCEPTED` events
- Fetch user email details from User Service
- Send email notifications to learner or mentor

Important classes:

- `SessionEventListener`
- `NotificationService`
- `NotificationServiceImpl`
- `UserClient`
- `SessionEvent`

## Config Server

Config Server provides centralized configuration to all services.

Responsibilities:

- Store common configuration externally
- Provide service-specific configuration
- Reduce repeated configuration inside each service

## Eureka Server

Eureka Server provides service discovery.

Responsibilities:

- Register all microservices
- Allow services to discover each other by service name
- Help API Gateway route requests using service names

## Common Module

The common module contains shared classes used across services.

Common classes:

- `ResponseDTO`
- `BadRequestException`
- `ResourceNotFoundException`
- `UnauthorizedException`

## Service Communication Summary

- Auth Service calls User Service during registration.
- Mentor Service calls Auth Service during mentor approval.
- Session Service calls User Service and Mentor Service for validation.
- Review Service calls Session Service before saving review.
- Notification Service calls User Service to fetch email details.
- Session Service publishes events to RabbitMQ.
- Notification Service consumes events from RabbitMQ.
