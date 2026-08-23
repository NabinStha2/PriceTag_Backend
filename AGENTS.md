# Repository Guidelines

## Project Structure & Module Organization

PriceTag Backend is a Java 21 Spring Boot e-commerce API. Source code is under `src/main/java/com/example/pricetag`. Domain modules in `features/` include auth, users, products, variants, categories, cart, orders, payments, media, and reviews. Each feature generally separates `controller`, `service`, `repository`, `entity`, `dto`, and `mapper` code. Shared security/configuration, exceptions, enums, and utilities live in top-level packages. Tests mirror production packages in `src/test/java`; configuration profiles live in `src/main/resources/application*.properties`. Do not edit generated/local data in `target/`, `production/`, or `mysql-data/`.

## Architecture & API Compatibility

Keep the flow `Controller -> Service -> Repository -> Entity`; map entities to request/response DTOs rather than returning JPA entities. Reuse `CommonResponseDto`, `ApplicationException`, `ApplicationExceptionHandler`, and existing MapStruct mappers. The Android client is in `../PriceTag_Mobile`. Before changing endpoints, authentication, or DTO fields, inspect its Retrofit interface and matching DTO/repository usage, then report any required mobile changes. Preserve transaction boundaries and locking behavior in checkout, inventory, order-expiry, and Stripe webhook flows.

## Build, Test, and Development Commands

- `./mvnw spring-boot:run` runs the API on port 8080 with the default `dev` profile.
- `./mvnw test -DskipTests=false` runs JUnit 5 and Mockito tests; `pom.xml` skips tests by default.
- `./mvnw clean package -DskipTests=false` creates a tested executable JAR in `target/`.
- `docker compose up --build` starts the API and MySQL, exposing them on ports 8000 and 3307.

Use the checked-in Maven wrapper and Java 21.

## Coding Style & Naming Conventions

Use four-space indentation, `PascalCase` classes, `camelCase` members, and `UPPER_SNAKE_CASE` constants. New package names should be lowercase; preserve legacy package names such as `features/Variant` when modifying existing code. Follow established suffixes: `Controller`, `Service`, `ServiceImpl`, `Repo`, `Mapper`, and `RequestDto`/`ResponseDto`. Keep controllers thin and constructor-inject dependencies. No formatter is configured, so match neighboring code and organize imports.

## Testing Guidelines

Name tests `*Test` or `*Tests` and methods by behavior, such as `paidOrderCannotBeCancelled`. Add service unit tests for success and failure paths; add Spring context/integration tests for wiring, security, or persistence changes. Do not claim compilation or test success without running the relevant Maven command.

## Commits, Pull Requests & Security

Use focused, concise commits, preferably scoped and imperative: `orders: handle expired pending orders`. Pull requests should describe behavior, verification, linked issues, and API/schema/configuration impacts; include sample requests and responses for endpoint changes. Never commit `.env`, JWT secrets, database credentials, or Stripe/Cloudinary keys. Treat webhook payload validation and authorization changes as security-sensitive.
