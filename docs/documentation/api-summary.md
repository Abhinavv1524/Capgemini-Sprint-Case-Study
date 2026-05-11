# API Summary

This document lists the important APIs used in the SkillSync backend. Most protected APIs require a JWT token in the `Authorization` header.

Header format:

```http
Authorization: Bearer <jwt-token>
```

The API Gateway validates the token and forwards user context to services using:

```http
X-User-Id
X-User-Role
```

## Auth Service APIs

Base path:

```text
/api/auth
```

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/register` | Register a new user |
| POST | `/login` | Login and generate JWT token |
| GET | `/test` | Test protected auth API |
| PUT | `/users/{userId}/promote-mentor` | Promote user to mentor, admin only |

## User Service APIs

Base path:

```text
/api/users
```

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/` | Create user profile |
| GET | `/{id}` | Get user by profile id |
| GET | `/auth/{authUserId}` | Get user by auth user id |
| GET | `/` | Get all users |
| PUT | `/{id}` | Update user profile |

## Mentor Service APIs

Base path:

```text
/api/mentors
```

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/apply` | Learner applies as mentor |
| PUT | `/{id}/approve` | Admin approves mentor application |
| PUT | `/{id}/reject` | Admin rejects mentor application |
| GET | `/{id}` | Get mentor by mentor id |
| GET | `/user/{userId}` | Get mentor by user id |
| GET | `/` | Get all mentors |
| PUT | `/{id}` | Update mentor profile |

## Skill Service APIs

Base path:

```text
/api/skills
```

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/` | Create skill, admin only |
| GET | `/` | Get all skills |
| GET | `/{id}` | Get skill by id |

## Session Service APIs

Base path:

```text
/api/sessions
```

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/` | Learner creates session request |
| PUT | `/{id}/accept` | Mentor accepts session |
| PUT | `/{id}/reject` | Mentor rejects session |
| PUT | `/{id}/cancel` | Learner cancels session |
| GET | `/user` | Get sessions of current user |
| GET | `/{id}` | Get session by id |

## Review Service APIs

Base path:

```text
/api/reviews
```

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/` | Learner adds review |
| GET | `/mentor/{mentorId}` | Get reviews by mentor |
| GET | `/mentor/{mentorId}/rating` | Get mentor average rating |

## Group Service APIs

Base path:

```text
/api/groups
```

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/` | Create group |
| POST | `/{id}/join` | Learner joins group |
| POST | `/{id}/leave` | Learner leaves group |
| GET | `/` | Get all groups |
| GET | `/{id}` | Get group by id |

## Notification Service APIs

Base path:

```text
/api/notifications
```

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/test` | Test notification processing using a session event |

Most notification flow is event-driven. Session Service publishes events to RabbitMQ, and Notification Service consumes them.

## Important End-To-End API Flows

## Registration And Login

1. `POST /api/auth/register`
2. Auth Service saves encrypted password.
3. Auth Service calls User Service to create profile.
4. `POST /api/auth/login`
5. Auth Service returns JWT token.

## Mentor Approval

1. Learner calls `POST /api/mentors/apply`.
2. Admin calls `PUT /api/mentors/{id}/approve`.
3. Mentor Service updates mentor status.
4. Mentor Service calls Auth Service to update role.

## Session Booking

1. Learner calls `POST /api/sessions`.
2. Session Service validates learner and mentor.
3. Session is saved with `REQUESTED` status.
4. Session event is published to RabbitMQ.
5. Notification Service sends email to mentor.

## Review And Rating

1. Learner calls `POST /api/reviews`.
2. Review Service validates session using Session Service.
3. Review is saved.
4. Average rating can be fetched using `/api/reviews/mentor/{mentorId}/rating`.
