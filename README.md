# Faculty Review System (FRS)

This repository contains a Spring Boot web application for a Faculty Review System. The project was previously named `BMS`; the UI and documentation were updated to reflect the new focus.

Quick start:
- Build: `./mvnw package`
- Run: `./mvnw spring-boot:run`

The login page template is at `src/main/resources/templates/login.html` and static assets (CSS, JS, images) live under `src/main/resources/static/`.

## Database migrations
- Schema changes are managed with Flyway scripts in `src/main/resources/db/migration`.
- Current baseline migration: `V1__create_app_users_table.sql`.
- JPA is configured with `spring.jpa.hibernate.ddl-auto=validate` to prevent silent schema drift.

## Authentication foundation
- User records live in `app_users`.
- Username and email are unique.
- Account flags: `enabled`, `account_locked`.
