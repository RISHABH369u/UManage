# UManage Backend (Foundation + Auth/User Module)

## Overview
UManage is being developed as an enterprise-grade University Management System.
This milestone establishes the backend foundation and implements Authentication + Authorization + Users/Roles/Permissions + Academic Core.

## Tech Stack
- Java 21
- Spring Boot 3
- Gradle
- PostgreSQL (compatible with Supabase Postgres)
- Flyway migrations

## Current Architecture
Package structure under `src/main/java/com/umanage`:
- `config`: web/application configuration
- `common`: shared API wrappers, base model, exception handling
- `security`: JWT, Spring Security config, auth filter, user details service
- `auth`: login/refresh DTOs, controller, service, refresh token persistence
- `users`: user/role/permission entities, repositories, services, mapper, controllers
- `academic`: departments, programs, semesters, courses, offerings/sections, prerequisites, faculty assignments
- `enrollment`: enrollment requests, approvals/rejections, registrations, student/admin workflows
- `exam`: exam orchestration domain (scheduling, room/invigilator planning, seat planning, OMR sheet metadata, scan-ready records)
- `audit`: audit log entity/repository/service
- `bootstrap`: startup seed for baseline admin + permissions

## Implemented API Endpoints (v1)
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/roles` (requires `ROLE_MANAGE`)
- `GET /api/v1/roles` (requires `ROLE_VIEW`)
- `POST /api/v1/users` (requires `USER_MANAGE`)
- `GET /api/v1/users` (requires `USER_VIEW`)
- `GET /api/v1/users/{id}` (requires `USER_VIEW`)
- `GET /api/v1/users/me` (requires `PROFILE_VIEW`)
- `PUT /api/v1/users/me` (requires `PROFILE_MANAGE`)
- `POST /api/v1/academic/departments` (requires `ACADEMIC_MANAGE`)
- `GET /api/v1/academic/departments` (requires `ACADEMIC_VIEW`)
- `POST /api/v1/academic/programs` (requires `ACADEMIC_MANAGE`)
- `GET /api/v1/academic/programs` (requires `ACADEMIC_VIEW`)
- `POST /api/v1/academic/semesters` (requires `ACADEMIC_MANAGE`)
- `GET /api/v1/academic/semesters` (requires `ACADEMIC_VIEW`)
- `POST /api/v1/academic/courses` (requires `ACADEMIC_MANAGE`)
- `GET /api/v1/academic/courses` (requires `ACADEMIC_VIEW`)
- `POST /api/v1/academic/offerings` (requires `ACADEMIC_MANAGE`)
- `GET /api/v1/academic/offerings` (requires `ACADEMIC_VIEW`)
- `POST /api/v1/academic/prerequisites` (requires `ACADEMIC_MANAGE`)
- `GET /api/v1/academic/prerequisites` (requires `ACADEMIC_VIEW`)
- `POST /api/v1/academic/faculty-assignments` (requires `ACADEMIC_MANAGE`)
- `GET /api/v1/academic/faculty-assignments` (requires `ACADEMIC_VIEW`)
- `POST /api/v1/enrollment/requests` (requires `ENROLLMENT_REQUEST`)
- `GET /api/v1/enrollment/requests/me` (requires `ENROLLMENT_VIEW`)
- `GET /api/v1/enrollment/registrations/me` (requires `ENROLLMENT_VIEW`)
- `GET /api/v1/enrollment/requests/pending` (requires `ENROLLMENT_APPROVE`)
- `POST /api/v1/enrollment/requests/{id}/approve` (requires `ENROLLMENT_APPROVE`)
- `POST /api/v1/enrollment/requests/{id}/reject` (requires `ENROLLMENT_APPROVE`)

## Security Notes
- JWT access and refresh tokens are both signed and validated.
- Refresh tokens are persisted and rotated on refresh.
- Sensitive operations (login, refresh, create user) are audit-logged.
- Configure environment variables in non-local environments:
  - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
  - `JWT_SECRET`, `JWT_ISSUER`, `JWT_ACCESS_EXPIRY_MINUTES`, `JWT_REFRESH_EXPIRY_DAYS`
  - `AUTH_REGISTRATION_DEFAULT_ROLE`
  - `BOOTSTRAP_ADMIN_EMAIL`, `BOOTSTRAP_ADMIN_PASSWORD`

## Database
- Flyway migration: `src/main/resources/db/migration/V1__init_auth_users_audit.sql`
- Flyway migration: `src/main/resources/db/migration/V2__init_academic_core.sql`
- Flyway migration: `src/main/resources/db/migration/V3__init_enrollment_workflow.sql`
- Flyway migration: `src/main/resources/db/migration/V4__init_exam_orchestration.sql`

## Run Locally
1. Ensure PostgreSQL is running and accessible.
2. Set env vars or use defaults from `application.yml`.
3. Run:
   ```bash
   ./gradlew bootRun
   ```

## Test
```bash
./gradlew test
```

## Assumptions (for this milestone)
- This milestone delivers foundational backend architecture and first core module only.
- Domain modules like admissions, timetable, and results will be added incrementally on top of this architecture.
