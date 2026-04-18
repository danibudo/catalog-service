> Note: this README.md file has been generated with Claude Code.
# catalog-service

Book catalog microservice for the **Library Management System** — a portfolio project demonstrating a production-style microservice architecture across multiple languages and frameworks.

> The full system includes an auth-service (TypeScript/Node.js), user-service (Kotlin/Spring Boot), loan-service (Kotlin/Spring Boot), notification-service (TypeScript/Node.js), and an API gateway (Spring Cloud Gateway). Each service owns its own database and communicates asynchronously via RabbitMQ or synchronously over REST.

## Overview

The catalog-service is the authority for all book catalog data in the system. It manages titles (logical books) and copies (physical instances held by the library), and participates in the loan saga as the copy reservation authority. When the loan-service approves a loan request, it is this service that finds an available copy, marks it as on loan, and confirms the reservation — or reports that no copies are available.

Role-based access is enforced at the service layer. The service trusts caller identity from headers injected by the API gateway (`X-User-Id`, `X-User-Role`) rather than validating JWTs directly. Read endpoints are publicly accessible; write endpoints require a librarian or super-admin role.

## Features

- **Title management** — create, search, update, and delete titles with metadata (ISBN, author, genre, publication year, description)
- **Copy management** — register physical copies under a title, update their condition, and decommission them
- **Availability tracking** — dynamically computes how many copies of a title are currently available for loan
- **Filtering** — search titles by author, genre, or availability
- **Deletion guards** — titles cannot be deleted if any of their copies are on loan; copies cannot be decommissioned while on loan
- **Copy reservation saga** — consumes `loan.copy_reservation_requested` events; atomically reserves a copy and publishes the outcome (`copy_reserved` or `copy_reservation_failed`)
- **Copy release saga** — consumes `loan.copy_release_requested` events; restores a copy to available and confirms via `copy_released`
- **Public catalog access** — browse and search endpoints are accessible without authentication

## Tech Stack

- **Runtime:** Java 21
- **Framework:** Spring Boot
- **Database:** PostgreSQL (Spring Data JPA, Flyway migrations)
- **Messaging:** RabbitMQ (Spring AMQP)
- **Validation:** Jakarta Bean Validation
- **Containerisation:** Docker (multi-stage Maven build, non-root user)

## API

Public read endpoints (`GET /titles`, `GET /titles/{id}`) do not require authentication headers. All other endpoints require `X-User-Id` (UUID) and `X-User-Role` (role string), injected by the API gateway after JWT validation.

| Method | Path | Roles | Description |
|---|---|---|---|
| `GET` | `/titles` | public | List/search titles; optional `author`, `genre`, `available` query params |
| `GET` | `/titles/{id}` | public | Get a title with its available copy count |
| `POST` | `/titles` | `librarian`, `super-admin` | Create a new title |
| `PATCH` | `/titles/{id}` | `librarian`, `super-admin` | Update title metadata |
| `DELETE` | `/titles/{id}` | `librarian`, `super-admin` | Delete a title (only if no copies are on loan) |
| `GET` | `/titles/{id}/copies` | `librarian`, `super-admin` | List all copies of a title with their status |
| `POST` | `/titles/{id}/copies` | `librarian`, `super-admin` | Register a new physical copy |
| `PATCH` | `/copies/{id}` | `librarian`, `super-admin` | Update a copy's condition |
| `DELETE` | `/copies/{id}` | `librarian`, `super-admin` | Decommission a copy (only if not on loan) |
| `GET` | `/health` | — | Actuator health check |

**Create title request**
```
POST /titles
Content-Type: application/json
X-User-Id: <uuid>
X-User-Role: librarian

{
  "isbn": "9780261102217",
  "title": "The Lord of the Rings",
  "author": "J.R.R. Tolkien",
  "genre": "Fantasy",
  "publication_year": 1954,
  "description": "Epic high-fantasy novel."
}
```

**Register copy request**
```
POST /titles/{id}/copies
Content-Type: application/json
X-User-Id: <uuid>
X-User-Role: librarian

{ "condition": "good" }
```

All response bodies use `snake_case` field names. The title response includes an `available_copies` count computed dynamically from the copy table.

## Messaging

The service publishes to the `catalog-service.events` RabbitMQ exchange and consumes from `loan-service.events`. Each queue has a corresponding dead-letter queue (DLQ) for messages that fail processing.

| Event published | Trigger | Description |
|---|---|---|
| `catalog.title_created` | `POST /titles` | A new title was added to the catalog |
| `catalog.title_updated` | `PATCH /titles/{id}` | Title metadata was updated |
| `catalog.title_deleted` | `DELETE /titles/{id}` | A title was removed from the catalog |
| `catalog.copy_registered` | `POST /titles/{id}/copies` | A new physical copy was registered |
| `catalog.copy_reserved` | `loan.copy_reservation_requested` received | A copy was successfully reserved for a loan |
| `catalog.copy_reservation_failed` | `loan.copy_reservation_requested` received | No available copy was found for the requested title |
| `catalog.copy_released` | `loan.copy_release_requested` received | A copy was returned to the available pool |

| Event consumed | Action |
|---|---|
| `loan.copy_reservation_requested` | Finds the first available copy of the requested title, marks it `on_loan`, publishes `copy_reserved` or `copy_reservation_failed` |
| `loan.copy_release_requested` | Sets the specified copy back to `available`, publishes `copy_released` |

The reservation and release handlers are `@Transactional` — a publish failure rolls back the copy status change so the message is nacked and retried. A "no copies available" outcome is acked normally (it is a valid business result, not a processing failure).

## Running Locally

The included `docker-compose.yml` starts the full stack: PostgreSQL (with `user_service`, `auth_service`, and `catalog_service` databases), RabbitMQ, the auth-service, user-service, and catalog-service.

```bash
docker compose up --build
```

The auth-service and user-service repositories must be present at the same directory level as this repository:

```
../auth-service
../user-service
./               ← this repo
```

The catalog-service will be available at `http://localhost:8081`. The RabbitMQ management UI is available at `http://localhost:15672` (guest / guest).

Database migrations run automatically on startup via Flyway.

A minimal dev compose is also included for local development without the full stack:

```bash
docker compose -f docker-compose.dev.yml up
```

This starts only a PostgreSQL instance on port `5432` with the `catalog_service` database, suitable for running the application directly with `./mvnw spring-boot:run`.

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `PORT` | `8081` | HTTP port |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/catalog_service` | JDBC URL for PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Database password |
| `SPRING_RABBITMQ_HOST` | `localhost` | RabbitMQ host |
| `SPRING_RABBITMQ_PORT` | `5672` | RabbitMQ port |
| `SPRING_RABBITMQ_USERNAME` | `guest` | RabbitMQ user |
| `SPRING_RABBITMQ_PASSWORD` | `guest` | RabbitMQ password |
| `RABBITMQ_PREFETCH` | `10` | Per-consumer prefetch count |
