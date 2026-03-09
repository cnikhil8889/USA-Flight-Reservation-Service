## USA Flight Reservation Service (Spring Boot, MySQL)

A production-style backend for managing flights, reservations, passengers, and baggage for a US domestic airline.  
The project includes both a REST API and an interactive console UI, plus configurable baggage pricing and clean separation between API, domain, and persistence layers.

---

## Table of Contents

- [Overview](#overview)
- [Core Features](#core-features)
- [Domain Model](#domain-model)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
  - [Database](#database)
  - [Baggage Pricing](#baggage-pricing)
- [Console Application](#console-application)
- [REST API (v1)](#rest-api-v1)
  - [Flights](#flights)
  - [Reservations](#reservations)
  - [Baggage Quotes](#baggage-quotes)
- [Validation & Error Handling](#validation--error-handling)
- [Testing](#testing)
- [Design Notes & Extensibility](#design-notes--extensibility)
- [Future Enhancements](#future-enhancements)

---

## Overview

This repository implements the backend for a simplified airline reservation system:

- Seeded with **US domestic routes** (e.g., JFK → LAX, LAX → ORD).
- Prices and baggage fees are expressed in **USD**.
- Exposes a versioned **REST API under `/api/v1`** for integration with frontends or other services.
- Ships with a **console-based UI** for quick manual interaction and demos.
- Uses **config-driven baggage pricing** so fee rules can be tuned without code changes.

---

## Core Features

### Flights

- List all available flights.
- Search by origin and destination, with optional departure filters.
- Track total seats and currently available seats.

### Reservations

- Create a reservation for a passenger on a specific flight.
- Automatically reserves a seat and adjusts availability.
- Stores baggage information (weight and type) and total price (flight fare + baggage fees).
- Supports cancellation, which releases a seat back to inventory.
- Generates human-friendly reservation codes (e.g., `AR-AB2F9K7P`).

### Passengers

- Basic passenger model stored separately and reused via reservations.
- Simple deduplication: passengers can be resolved by email or passport/government ID.

### Baggage

- Supports carry-on, checked, and overweight baggage.
- Automatically upgrades baggage type based on actual weight.
- Pricing and rules are configurable via `application.properties`.
- Provides both a console-based calculator and a REST endpoint for quotes.

---

## Domain Model

Entity classes live under `com.airline.model`:

- `Flight`
  - `flightNumber`, `origin`, `destination`
  - `departureTime`, `arrivalTime`
  - `price` (BigDecimal, USD)
  - `totalSeats`, `availableSeats`

- `Passenger`
  - `firstName`, `lastName`
  - `email`, `phoneNumber`
  - `passportNumber` (or other government ID)

- `Reservation`
  - `reservationCode` (unique, code-like `AR-XXXXXXXX`)
  - `flight` (many-to-one)
  - `passenger` (many-to-one)
  - `bookingDate`
  - `totalAmount` (BigDecimal, USD)
  - `status` (`CONFIRMED`, `CANCELLED`, `CHECKED_IN`, `COMPLETED`)
  - `baggageWeight`, `baggageType`

- `Baggage` (value entity used in calculations)
  - `weight`, `type`, `charge`, `description`

- Enums:
  - `ReservationStatus`
  - `BaggageType` (`CARRY_ON`, `CHECKED`, `OVERWEIGHT`)

---

## Architecture

The codebase follows a layered architecture:

- **API Layer** (`com.airline.api.controller`, `com.airline.api.dto`, `com.airline.api.error`)
  - `FlightV1Controller`, `ReservationV1Controller`, `BaggageV1Controller`
  - DTOs for input/output (no JPA entities leaked over the wire).
  - Global error handler with a consistent JSON error format.

- **Console Layer** (`com.airline.console`)
  - `ConsoleApplication` (a `CommandLineRunner`) provides a menu-driven terminal UI.
  - Uses the same services as the REST API to keep business logic centralized.

- **Service Layer** (`com.airline.service`)
  - `FlightService` – read/query flight data.
  - `ReservationService` – reservation lifecycle (create, list, cancel).
  - `BaggageService` – baggage type resolution and fee calculation.
  - `PassengerService` – resolves passengers by email/passport and avoids duplicates.
  - `SeatInventoryService` – central point for reserving and releasing seats.
  - `ReservationCodeGenerator` – generates unique, collision-resistant reservation codes.

- **Persistence Layer** (`com.airline.repository`)
  - Spring Data JPA repositories for `Flight`, `Passenger`, `Reservation`.

- **Configuration** (`com.airline.config`)
  - `DataInitializer` seeds sample US flight data when the database is empty.
  - `CorsConfig` enables CORS for a potential frontend.
  - `BaggagePricingProperties` binds baggage pricing settings from configuration.

This structure makes it straightforward to:
- Swap out the API layer (e.g., GraphQL or gRPC).
- Integrate with cloud services (RDS, SSM, CloudWatch, SNS/SQS, etc.).
- Extend features without tangling business and transport concerns.

---

## Tech Stack

- **Language**: Java 17  
- **Framework**: Spring Boot 3.x  
- **Web**: Spring Web (REST controllers)  
- **Persistence**: Spring Data JPA (Hibernate) + Flyway migrations  
- **Database**: MySQL 8.x  
- **Validation**: Jakarta Bean Validation  
- **Observability**: Spring Boot Actuator + Micrometer  
- **API Documentation**: springdoc-openapi (Swagger UI)  
- **Build Tool**: Maven  
- **Testing**:
  - JUnit 5
  - Service-level unit tests for baggage pricing and reservation code generation

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+
- MySQL 8.0+ (or compatible)

### 1. Clone and configure

```bash
git clone <your-fork-or-repo-url>
cd USA-Flight-Reservation-Service
```

Update `src/main/resources/application.properties` with your local database credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/airline_reservation?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 2. Run the application

```bash
mvn spring-boot:run
```

The application starts on:

- Backend: `http://localhost:8080`

On startup, sample US domestic flights are automatically inserted if the `flights` table is empty.

---

## Configuration

### Database

Relevant properties in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/airline_reservation?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.format_sql=true
```

Flyway is configured via the default `db/migration` location. The initial schema is applied from:

- `src/main/resources/db/migration/V1__initial_schema.sql`

For local development, `ddl-auto=update` can still help with minor tweaks, but in a production-style setup you would rely on Flyway migrations as the primary source of truth.

### Baggage Pricing

Baggage pricing is configuration-driven via `BaggagePricingProperties`:

```properties
# US-style defaults, in USD
airline.baggage.pricing.checked-base-fee=35.00
airline.baggage.pricing.overweight-per-kg=50.00
```

The rules are:

- Carry-on:
  - Up to 7 kg (≈ 15 lb) – free.
- Checked:
  - Up to 23 kg (≈ 50 lb) – base fee (`checked-base-fee`).
- Overweight:
  - Above 23 kg – base fee + (`overweight-per-kg` × excess kg).
- If the intended type cannot accommodate the weight, it is automatically upgraded (e.g., from CARRY_ON to CHECKED, from CHECKED to OVERWEIGHT).

Both the console UI and the REST API use this configuration.

---

## Observability & Health

Spring Boot Actuator is enabled to provide basic health and metrics endpoints:

- `GET /actuator/health` – liveness/readiness-style health check.
- `GET /actuator/metrics` – base JVM/HTTP metrics (exposed minimally by default).

A custom Micrometer counter is recorded whenever a reservation is created:

- Metric name: `reservations.created.total`

This makes it straightforward to plug into monitoring systems (e.g., Amazon CloudWatch, Prometheus) and build dashboards such as reservations-per-minute or error rates.

### AWS Observability & Configuration (Optional)

When running the application in an AWS-oriented profile (for example the `aws-dev` profile), additional integrations become available:

- **CloudWatch metrics**  
  A dedicated `CloudWatchMeterRegistry` (via `CloudWatchMetricsConfig`) forwards Micrometer metrics, including `reservations.created.total`, to Amazon CloudWatch.  
  - Controlled via properties such as:
    - `aws.region` (or `AWS_REGION` env var)
    - `aws.cloudwatch.namespace` (default: `AirlineReservation`)
    - `aws.cloudwatch.step` (default: `60s`)

- **Structured JSON logging for CloudWatch Logs**  
  A `logback-spring.xml` configuration emits JSON logs including timestamp, level, logger, message, exception, and MDC fields. This format is friendly to CloudWatch Logs and other log aggregation tools.

- **AWS Parameter Store-backed configuration**  
  An optional `AwsParameterStoreEnvironmentPostProcessor` can pull selected configuration values from AWS Systems Manager Parameter Store when `aws.parameterstore.enabled=true`.  
  - Example parameters it looks for (all optional, with local defaults as fallback):
    - `/airline-reservation/spring.datasource.url` → `spring.datasource.url`
    - `/airline-reservation/spring.datasource.username` → `spring.datasource.username`
    - `/airline-reservation/spring.datasource.password` → `spring.datasource.password`
    - `/airline-reservation/airline.baggage.pricing.checked-base-fee` → `airline.baggage.pricing.checked-base-fee`
    - `/airline-reservation/airline.baggage.pricing.overweight-per-kg` → `airline.baggage.pricing.overweight-per-kg`

These integrations are **opt-in** and safe for local development: if AWS credentials or parameters are not available, the application simply falls back to the existing properties in `application.properties`.

---

## Console Application

The console UI (`ConsoleApplication`) starts automatically with the Spring Boot app and presents a main menu:

1. View All Available Flights  
2. Search Flights  
3. Make Reservation  
4. View Reservation  
5. Calculate Baggage Charge  
6. View All Reservations  
7. Cancel Reservation  
8. View Baggage Policy  
0. Exit  

Key behaviors:

- **View All Available Flights**  
  Lists US flights (e.g., JFK → LAX) with IDs, flight numbers, departure times, and prices in **USD**.

- **Make Reservation**  
  - Prompts for a flight ID.  
  - Collects passenger information (name, email, phone, ID).  
  - Optionally collects baggage details (weight and intended type).  
  - Uses `ReservationService` and `BaggageService` to calculate the final total.  
  - Prints a confirmation including reservation code, route, baggage info, and total amount in USD.

- **Calculate Baggage Charge**  
  - Interactively calculates baggage charges and shows how weight affects type and pricing, using the same logic as the API.

The console is useful for quick demos and manual QA while still exercising the same service layer as the REST API.

---

## REST API (v1)

All versioned endpoints live under `/api/v1` and use DTOs defined in `com.airline.api.dto`.  
Legacy, entity-returning controllers under `/api/...` are deprecated in favor of this v1 API.

### Flights

Base path: `/api/v1/flights` (`FlightV1Controller`)

- `GET /api/v1/flights`  
  List flights, with optional filters:

  - `origin`: origin city/airport name (e.g., `New York (JFK)`)
  - `destination`: destination city/airport name (e.g., `Los Angeles (LAX)`)
  - `departureFrom`: ISO date-time (`YYYY-MM-DDTHH:mm:ss`)
  - `onlyAvailable`: `true` (default) to exclude fully booked flights

  Example:

  ```http
  GET /api/v1/flights?origin=New%20York%20(JFK)&destination=Los%20Angeles%20(LAX)
  ```

- `GET /api/v1/flights/{flightId}`  
  Returns detailed information (including `totalSeats` and `availableSeats`) for a single flight.

### Reservations

Base path: `/api/v1/reservations` (`ReservationV1Controller`)

- `POST /api/v1/reservations`  
  Create a reservation.

  Request body:

  ```json
  {
    "flightId": 1,
    "passenger": {
      "firstName": "Alex",
      "lastName": "Johnson",
      "email": "alex.johnson@example.com",
      "phoneNumber": "+14085551234",
      "passportNumber": "X1234567"
    },
    "baggageWeight": 18.0,
    "baggageType": "CHECKED"
  }
  ```

  Response:

  - Reservation details including generated code, status, total amount, flight summary, and passenger summary.

- `GET /api/v1/reservations/{reservationId}`  
  Fetch a reservation by its numeric ID.

- `GET /api/v1/reservations/by-code/{code}`  
  Fetch a reservation by its reservation code (e.g., `AR-AB2F9K7P`).

- `POST /api/v1/reservations/{reservationId}/cancel`  
  Cancel a reservation. This sets the reservation status to `CANCELLED` and releases one seat on the associated flight.

- `GET /api/v1/reservations?status=&email=`  
  Filter reservations:

  - `status`: optional, one of `CONFIRMED`, `CANCELLED`, `CHECKED_IN`, `COMPLETED`.
  - `email`: optional passenger email.

### Baggage Quotes

Base path: `/api/v1/baggage` (`BaggageV1Controller`)

- `POST /api/v1/baggage/quote`  
  Calculate baggage charges and see how the system will classify the baggage.

  Request body:

  ```json
  {
    "weight": 26.0,
    "requestedType": "CHECKED"
  }
  ```

  Response:

  - `weight` (kg)
  - `requestedType`, `actualType`
  - `baseFee`, `overweightFee`, `totalFee`
  - `description` explaining any upgrade and excess weight

---

## API Documentation (OpenAPI / Swagger)

The REST API is documented using springdoc-openapi. When the application is running, you can explore and try endpoints via Swagger UI:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

This UI reflects the `/api/v1` controllers and DTOs and is useful for manual testing or sharing the contract with other teams.

---

## Validation & Error Handling

Validation is based on Jakarta Bean Validation annotations across entities and DTOs.

The API uses a centralized `GlobalExceptionHandler` (`com.airline.api.error`):

- Returns a consistent JSON `ApiErrorResponse` shape:
  - `timestamp`, `status`, `error`, `message`, `path`, `details`
- Handles:
  - Request body validation errors (`MethodArgumentNotValidException`, `ConstraintViolationException`)
  - Resource not found (`ResourceNotFoundException`)
  - Conflicts such as no available seats (`ConflictException`)
  - Malformed JSON and unexpected server errors

This results in predictable, client-friendly responses for both happy paths and failures.

---

## Testing

Unit tests live under `src/test/java` and include:

- `BaggageServiceTest`
  - Verifies free carry-on behavior.
  - Verifies checked baggage uses base fee.
  - Verifies overweight baggage applies per-kg charges correctly.

- `ReservationCodeGeneratorTest`
  - Verifies reservation codes have the correct prefix and length.
  - Checks that subsequent codes are typically unique.

You can run tests with:

```bash
mvn test
```

Additional service and integration tests can be added to cover reservation flows, seat inventory behavior, and REST endpoints.

---

## Design Notes & Extensibility

This project is structured to be easy to extend in several directions:

- **Cloud deployment**: The config structure (especially for baggage pricing) and clear layering make it straightforward to plug in:
  - Amazon RDS (MySQL)
  - Parameter Store / Secrets Manager for configuration
  - CloudWatch Logs and metrics
  - SNS/SQS for reservation events
- **Frontends**: The DTO-based `/api/v1` contract is designed for integration with:
  - Single-page apps (React, Vue, Angular)
  - Native mobile apps
  - Other backend services

In this codebase, several AWS-oriented extension points are already wired:

- **Observability & configuration**: CloudWatch metrics integration, JSON logging via `logback-spring.xml`, and optional AWS Systems Manager Parameter Store configuration loading.
- **Events & notifications**: A pluggable `ReservationEventPublisher` (with `NoOpReservationEventPublisher` and `SnsReservationEventPublisher`) and `NotificationPublisher` (with `LoggingNotificationPublisher`) allow reservation events to flow to AWS SNS and user-facing channels.
- **Security**: Optional JWT-based resource server security (e.g., Amazon Cognito) is enabled when the `aws-auth` profile is active, leaving local development behavior unchanged when it is not.

Because the console UI reuses the service layer, it remains a good way to manually test changes as the surface area grows.

---

## Future Enhancements

Some natural next steps:

- Database migrations with Flyway or Liquibase.
- More comprehensive test coverage for reservation flows and error cases.
- Integration with payment providers (simulated or real).
- Notification flows (email/SMS) triggered on reservation events.
- Fine-grained seat selection and seat map modeling.
- Authentication and role-based access control for admin vs. customer APIs.

This project is intentionally small but realistic, designed to demonstrate solid backend design, configurability, and readiness for cloud deployment.