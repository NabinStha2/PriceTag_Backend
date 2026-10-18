# PriceTag Backend

A REST API for an e-commerce application, built with Java 21 and Spring Boot. It provides the backend services for product discovery, customer accounts, shopping carts, checkout, Stripe payments, and order management.

> This repository contains the backend API. The mobile client is maintained separately.

## Features

- JWT-based authentication, registration verification by email OTP, refresh tokens, and password recovery
- Product catalog with categories, subcategories, variants, search, pagination, and image management
- Customer profiles, addresses, shopping carts, and product reviews
- Order checkout with server-side price calculation, idempotency keys, stock reservation, and order expiry handling
- Stripe PaymentIntent creation and signed webhook processing
- DTO-based API responses, centralized exception handling, and OpenAPI/Swagger UI

## Technology

- Java 21, Spring Boot 4, Spring MVC, Spring Security
- Spring Data JPA / Hibernate and MySQL
- JWT, Stripe, Cloudinary, and Spring Mail
- MapStruct, Lombok, Maven
- Docker Compose for local API and database startup

## Architecture

The code is organized by business feature under `src/main/java/com/example/pricetag/features`. Feature code generally follows a controller → service → repository → entity flow, with request and response DTOs at the API boundary. Shared configuration, security, exceptions, and utilities are in top-level packages.

## Requirements

- Java 21
- Docker Desktop with Docker Compose, or a local MySQL server
- API credentials for email, Cloudinary, Stripe, and JWT signing

## Run locally with Docker Compose

1. Create a `.env` file in the project root. Keep it local and do not commit it:

   ```dotenv
   MYSQL_LOCAL_DATABASE=pricetag
   MYSQL_LOCAL_PASSWORD=choose-a-local-password
   JWT_SECRET=replace-with-a-long-random-secret

   EMAIL_HOST=smtp.example.com
   EMAIL_PORT=587
   EMAIL_USERNAME=your-email-username
   EMAIL_PASSWORD=your-email-password

   CLOUDINARY_CLOUD_NAME=your-cloud-name
   CLOUDINARY_API_KEY=your-cloudinary-api-key
   CLOUDINARY_API_SECRET=your-cloudinary-api-secret

   STRIPE_PUBLISHABLE_KEY=your-stripe-publishable-key
   STRIPE_SECRET_KEY=your-stripe-secret-key
   STRIPE_WEBHOOK_SECRET=your-stripe-webhook-secret
   ```

2. Start the API and MySQL:

   ```bash
   docker compose up --build
   ```

The API is available at `http://localhost:8000`; MySQL is exposed on port `3307`. Compose maps the API's internal port `8080` to host port `8000`.

## Run with the Maven wrapper

Start MySQL and provide the variables above in your environment or `.env`, then run:

```bash
./mvnw spring-boot:run
```

By default, the `dev` profile uses MySQL at `localhost:3306`. Override the database settings with `MYSQL_LOCAL_PORT`, `MYSQL_LOCAL_DATABASE`, `MYSQL_LOCAL_USER`, and `MYSQL_LOCAL_PASSWORD` as needed. The API listens on port `8080` by default; set `PORT` to change it.

## API documentation

With the API running, open Swagger UI at [`http://localhost:8000/swagger-ui/index.html`](http://localhost:8000/swagger-ui/index.html) when using Docker Compose, or replace port `8000` with `8080` for a local Maven run. OpenAPI JSON is available at `/v3/api-docs`.

Most application endpoints are under `/api/v1`. Authentication and the Stripe webhook have public access where configured; other endpoints may require a bearer access token. The Swagger UI describes the available routes and request models.

## Tests

Run the Maven test suite with:

```bash
./mvnw test -DskipTests=false
```

The Maven configuration skips tests by default, so `-DskipTests=false` is needed to execute them.

## Project layout

```text
src/main/java/com/example/pricetag/
├── config/       # Spring, security, JWT, Stripe, email, and API documentation config
├── exceptions/   # Shared API exception handling
├── features/     # Feature modules and their controllers, services, repositories, DTOs, and entities
├── utils/        # Shared utilities
└── ...           # Application entry point and shared DTOs
```

## Configuration and security

Credentials are read from environment variables (or the optional root `.env` file); never commit real credentials. The development profile configures Hibernate schema updates for local development. Review the production profile and set deployment-specific values before deploying.

## API client

The Postman collection and local environment are included in `postman/collections/PriceTag Backend` and `postman/environments`. Set the collection's base URL and credentials before sending authenticated requests.

## License

No license is currently specified. Add a `LICENSE` file before redistributing this project under an open-source license.
