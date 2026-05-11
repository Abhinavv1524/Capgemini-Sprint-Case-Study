# SkillSync Project Overview

## Introduction

SkillSync is a microservices-based learning and mentorship platform. The project connects learners with mentors so that learners can discover skills, book mentorship sessions, join learning groups, and submit reviews after sessions.

The system has three main user roles:

- Learner
- Mentor
- Admin

The backend is built using Java, Spring Boot, Spring Cloud, JWT security, MySQL, RabbitMQ, OpenFeign, Eureka Server, Config Server, and monitoring tools like Zipkin, Loki, and Grafana.

## Problem Statement

Learners often need guidance from experienced people, but there should be a structured platform where they can find mentors, request sessions, track session status, and review mentors. SkillSync solves this by providing a role-based mentorship system with session booking, mentor approval, reviews, groups, and notifications.

## Project Objective

The objective of SkillSync is to create a modular backend system where each major feature is handled by a separate microservice. This makes the project easier to maintain, test, and scale.

Main objectives:

- Allow users to register and login securely.
- Manage learner and mentor profiles.
- Allow learners to apply as mentors.
- Allow admins to approve or reject mentor applications.
- Allow admins to manage skills.
- Allow learners to book sessions with mentors.
- Allow mentors to accept or reject sessions.
- Send notifications for important session events.
- Allow learners to review mentors.
- Allow users to create and join groups.

## Users And Roles

### Learner

A learner can register, login, manage profile, view skills, search mentors, apply as mentor, book sessions, join groups, submit reviews, and view notifications.

### Mentor

A mentor can login, manage mentor profile, view session requests, accept or reject sessions, view reviews, and receive notifications.

### Admin

An admin can login, manage skills, view users, approve mentor applications, reject mentor applications, and monitor system data.

## Technology Stack

- Java
- Spring Boot
- Spring Cloud Gateway
- Spring Cloud Config Server
- Eureka Discovery Server
- Spring Security
- JWT
- Spring Data JPA
- MySQL
- OpenFeign
- RabbitMQ
- Docker
- Zipkin
- Grafana
- Loki
- Angular frontend

## High-Level Architecture

The frontend sends all requests to the API Gateway. The API Gateway validates JWT tokens and routes requests to the correct microservice. Eureka Server is used for service discovery, and Config Server provides centralized configuration. Each microservice owns its own database. Services communicate synchronously using Feign clients and asynchronously using RabbitMQ events.

## Main Backend Services

- API Gateway
- Auth Service
- User Service
- Mentor Service
- Skill Service
- Session Service
- Review Service
- Group Service
- Notification Service
- Config Server
- Eureka Server
- Common Module

## Key Features

- JWT-based authentication
- Role-based authorization
- Microservices architecture
- Database per service
- Service discovery using Eureka
- Centralized configuration
- Feign-based inter-service communication
- RabbitMQ-based event-driven notifications
- Review and rating system
- Group management
- Monitoring and tracing support

## Viva Summary

SkillSync is a complete microservices backend for a mentorship platform. It separates responsibilities into different services like authentication, users, mentors, sessions, reviews, groups, and notifications. The project uses JWT for security, Eureka for service discovery, Config Server for centralized configuration, Feign for service communication, and RabbitMQ for asynchronous notification events.
