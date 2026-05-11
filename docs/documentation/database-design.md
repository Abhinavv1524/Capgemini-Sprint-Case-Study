# Database Design

SkillSync follows a microservices architecture, so each major service owns its own database. The ER diagram in `docs/diagrams/ER Diagram.png` shows the logical relationship between the main entities.

## Main Entities

## AuthUser

`AuthUser` stores authentication-related user data.

Important fields:

- `user_id`
- `name`
- `email`
- `password`

Purpose:

- Stores login credentials.
- Used by Auth Service for registration and login.
- Password is stored in encrypted form.

## Role

`Role` stores user roles.

Important fields:

- `role_id`
- `role_name`

Example roles:

- `ROLE_ADMIN`
- `ROLE_LEARNER`
- `ROLE_MENTOR`

Purpose:

- Used for role-based authorization.
- Helps decide which API a user can access.

## UserProfile

`UserProfile` stores profile details of a user.

Important fields:

- `profile_id`
- `auth_user_id`
- `name`
- `email`
- `bio`
- `skills`
- `role`

Purpose:

- Separates profile data from login credentials.
- Used by User Service.

## Mentor

`Mentor` stores mentor-specific details.

Important fields:

- `mentor_id`
- `user_id`
- `bio`
- `experience`
- `hourly_rate`
- `status`

Possible statuses:

- `PENDING`
- `APPROVED`
- `REJECTED`

Purpose:

- Stores mentor application and mentor profile details.
- Admin approval changes status from `PENDING` to `APPROVED` or `REJECTED`.

## Skill

`Skill` stores skills available in the system.

Important fields:

- `skill_id`
- `skill_name`
- `created_at`

Purpose:

- Admin can create skills.
- Learners can view skills.

## Session

`Session` stores mentorship session booking data.

Important fields:

- `session_id`
- `learner_id`
- `mentor_id`
- `session_time`
- `status`
- `created_at`

Possible statuses:

- `REQUESTED`
- `ACCEPTED`
- `REJECTED`
- `CANCELLED`
- `COMPLETED`

Purpose:

- Tracks learner and mentor session requests.
- Used by Session Service.

## Review

`Review` stores mentor feedback given by learners.

Important fields:

- `review_id`
- `mentor_id`
- `user_id`
- `session_id`
- `rating`
- `comment`
- `created_at`

Purpose:

- Stores rating and comment for a mentor.
- Used to calculate mentor average rating.

## Group

`Group` stores learning group details.

Important fields:

- `group_id`
- `group_name`
- `description`
- `created_by`

Purpose:

- Allows users to create learning groups.

## GroupMember

`GroupMember` stores group membership details.

Important fields:

- `group_member_id`
- `group_id`
- `user_id`

Purpose:

- Connects users with groups.
- Supports many users joining many groups.

## Important Relationships

- One `AuthUser` owns one `UserProfile`.
- One `AuthUser` has a `Role`.
- One `UserProfile` can become one `Mentor`.
- One `UserProfile` can book many `Session` records.
- One `Mentor` can provide many `Session` records.
- One `UserProfile` can write many `Review` records.
- One `Session` can have one review.
- One `Mentor` can receive many reviews.
- One `UserProfile` can create many groups.
- One `Group` can have many `GroupMember` records.
- One `UserProfile` can join many groups through `GroupMember`.

## Database Per Service

SkillSync uses separate databases logically for different services:

- Auth Service uses Auth DB.
- User Service uses User DB.
- Mentor Service uses Mentor DB.
- Skill Service uses Skill DB.
- Session Service uses Session DB.
- Review Service uses Review DB.
- Group Service uses Group DB.

## Viva Explanation

The database design separates authentication, profile, mentor, session, review, and group data. This matches the microservices design because each service owns the data related to its responsibility. The ERD shows how users, mentors, sessions, reviews, and groups are connected.
