# 🛒 Store — E-Commerce REST API

A production-ready e-commerce backend built with **Spring Boot 4.0.6** and **Java 25**. It provides a complete shopping platform: user authentication, product catalog, a guest-friendly cart system, order management, and real payment processing via **Stripe Checkout**.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture Overview](#architecture-overview)
- [Database Schema](#database-schema)
- [API Endpoints](#api-endpoints)
- [Security Model](#security-model)
- [Payment Flow](#payment-flow)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [Project Structure](#project-structure)

---

## Features

### Authentication & Identity
- JWT-based authentication with a **dual-token strategy**: short-lived access tokens (returned in the response body) and long-lived refresh tokens (stored in a secure **HttpOnly cookie**)
- Token refresh endpoint that silently re-issues access tokens without re-login
- BCrypt password hashing
- Role-based access control — `USER` and `ADMIN` roles

### User Management
- User registration with duplicate email detection
- Extended user profiles: bio, phone number, date of birth, and **loyalty points**
- Multiple shipping addresses per user account
- Personal **wishlist** (many-to-many relationship with products)
- Sortable user listings (by `name` or `email`)

### Product Catalog
- Full CRUD for products and categories
- Filter products by category ID
- Price integrity enforced at the database level (`price > 0.00`)

### Cart System
- **Guest-friendly**: carts can be created and used without authentication
- Add items — duplicate additions auto-increment quantity instead of creating a duplicate row
- Update item quantities in place
- Remove individual items or clear the entire cart

### Orders
- Atomic checkout: cart → order creation → Stripe session → cart clear, all within a single transaction. If the Stripe session fails, the order is rolled back.
- Order **ownership enforcement**: authenticated users can only view their own orders
- Order status lifecycle: `PENDING` → `PAYED` / `FAILED` / `CANCELED`

### Payment Processing (Stripe)
- Creates a **Stripe Checkout Session** with line items mapped directly from the order
- Stores the internal `order_id` in Stripe's `PaymentIntent` metadata for reliable webhook reconciliation
- Webhook endpoint verifies the **Stripe-Signature** header before processing events
- Handles `payment_intent.succeeded` and `payment_intent.payment_failed` events to update order status

### Developer Experience
- **Swagger UI** at `/swagger-ui.html` via SpringDoc OpenAPI
- **Liquibase** database migrations — schema is versioned and reproducible across environments
- **MapStruct** compile-time DTO mapping — zero runtime reflection overhead
- Custom `@Lowercase` Bean Validation constraint
- Request/response logging filter on every endpoint

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.0.6 |
| Security | Spring Security + JJWT 0.13.0 |
| Database | PostgreSQL |
| Migrations | Liquibase |
| ORM | Spring Data JPA (Hibernate) |
| DTO Mapping | MapStruct 1.6.3 |
| Payments | Stripe Java SDK 32.1.0 |
| API Docs | SpringDoc OpenAPI 3.0.2 |
| Utilities | Lombok, spring-dotenv |
| Build | Maven |

---

## Architecture Overview

The project follows a standard layered Spring Boot architecture:

```
Controllers  →  Services  →  Repositories  →  Database
     ↕               ↕
   DTOs          Entities
     ↕
  Mappers (MapStruct)
```

The `payment` package is intentionally isolated from the core domain. The `PaymentGateway` interface abstracts the Stripe implementation, making it straightforward to swap payment providers without touching business logic.

```
com.leroy.store
├── config/         # Security & JWT configuration beans
├── controllers/    # REST endpoints (Auth, User, Product, Cart, Order, Checkout, Admin)
├── dtos/           # Request/response data transfer objects
├── entities/       # JPA entities (User, Product, Cart, Order, Profile, Address, ...)
├── exceptions/     # Domain exceptions + GlobalExceptionHandler
├── filters/        # JWT authentication filter, logging filter
├── mappers/        # MapStruct mapper interfaces
├── payment/        # Payment abstraction: PaymentGateway, StripePaymentGateway, CheckoutService
├── repositories/   # Spring Data JPA repositories with custom JPQL queries
├── services/       # Business logic (Auth, Cart, Order, JWT, User)
└── validation/     # Custom constraint annotations (@Lowercase)
```

---

## Database Schema

Schema is managed by **Liquibase** across five versioned changelogs.

### Tables

**`users`** — Core account table
| Column | Type | Notes |
|---|---|---|
| id | UUID | PK, auto-generated |
| name | VARCHAR(255) | |
| email | VARCHAR(255) | UNIQUE |
| password | TEXT | BCrypt hashed |
| role | VARCHAR(20) | `USER` or `ADMIN` |

**`profiles`** — One-to-one extension of users
| Column | Type | Notes |
|---|---|---|
| id | UUID | PK, FK → users(id) |
| bio | TEXT | |
| phone_number | VARCHAR(15) | |
| date_of_birth | DATE | |
| loyalty_points | INTEGER | CHECK >= 0, default 0 |

**`addresses`** — Many-to-one with users
| Column | Type |
|---|---|
| id | UUID PK |
| street, city, state, zip | VARCHAR |
| user_id | UUID FK → users |

**`categories`** / **`products`**
| products column | Notes |
|---|---|
| price | NUMERIC(10,2), CHECK > 0 |
| category_id | FK → categories |

**`wishlist`** — Many-to-many join table (users ↔ products)

**`carts`** / **`cart_items`**
- `cart_items` has a `UNIQUE(cart_id, product_id)` constraint — enforces the auto-increment-on-duplicate behaviour at the DB level

**`orders`** / **`order_items`**
| Column | Notes |
|---|---|
| status | PENDING / PAYED / FAILED / CANCELED |
| total_price | Snapshot at time of checkout |
| unit_price (order_items) | Price snapshot per item |

---

## API Endpoints

### Auth — `/auth`
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/auth/login` | Public | Returns access token; sets refresh cookie |
| POST | `/auth/refresh` | Public (cookie) | Re-issues a new access token |
| GET | `/auth/me` | Authenticated | Returns the authenticated user's profile |

### Users — `/users`
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/users` | Public | Register a new user |
| GET | `/users` | Authenticated | List all users (sortable by `name` or `email`) |
| GET | `/users/{id}` | Authenticated | Get a single user |
| PUT | `/users/{id}` | Authenticated | Update a user |
| DELETE | `/users/{id}` | Authenticated | Delete a user |

### Products — `/products`
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/products` | Authenticated | List all products (optional `?categoryId=` filter) |
| GET | `/products/{id}` | Authenticated | Get a single product |
| POST | `/products` | Authenticated | Create a product |
| PUT | `/products/{id}` | Authenticated | Update a product |
| DELETE | `/products/{id}` | Authenticated | Delete a product |

### Carts — `/carts`
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/carts` | **Public** | Create a new guest cart |
| GET | `/carts/{cartId}` | **Public** | Retrieve cart with items and total |
| POST | `/carts/{cartId}/items` | **Public** | Add item (auto-increments if already in cart) |
| PUT | `/carts/{cartId}/items/{productId}` | **Public** | Update item quantity |
| DELETE | `/carts/{cartId}/items/{productId}` | **Public** | Remove a single item |
| DELETE | `/carts/{cartId}/items` | **Public** | Clear entire cart |

### Checkout — `/checkout`
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/checkout` | Authenticated | Creates order + Stripe Checkout Session |
| POST | `/checkout/webhook` | **Public** (Stripe signed) | Handles Stripe payment events |

### Orders — `/orders`
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/orders` | Authenticated | List the authenticated user's orders |
| GET | `/orders/{orderId}` | Authenticated | Get order detail (ownership enforced) |

### Admin — `/admin`
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/admin/hello` | **ADMIN role** | Protected admin endpoint |

---

## Security Model

```
Public           → /carts/**, POST /users, /auth/login, /auth/refresh, /checkout/webhook
Authenticated    → /auth/me, /products/**, /orders/**, POST /checkout
ADMIN role only  → /admin/**
```

- All sessions are **stateless** (no server-side session storage)
- CSRF protection is disabled (JWT-authenticated API, no browser form submissions)
- Unauthenticated requests to protected routes return **401 Unauthorized**
- Authenticated users accessing admin routes receive **403 Forbidden**
- The refresh token cookie is `HttpOnly`, `Secure`, and scoped to `/`

---

## Payment Flow

```
Client                      Backend                        Stripe
  |                            |                              |
  |-- POST /checkout --------> |                              |
  |   { cartId }               |-- Create Order (PENDING) --> DB
  |                            |-- Session.create() --------> |
  |                            |<-- { url } ----------------- |
  |                            |-- Clear cart -------------> DB
  |<-- { orderId, url } ------ |                              |
  |                            |                              |
  |-- Redirect to Stripe URL   |                              |
  |                            |                              |
  |                            |<-- Webhook (signed) -------- |
  |                            |   payment_intent.succeeded   |
  |                            |-- Update Order → PAYED --> DB
```

If `Session.create()` throws a Stripe exception, the created order is **deleted** within the same transaction, ensuring no orphaned PENDING orders exist.

---

## Getting Started

### Prerequisites

- Java 25+
- PostgreSQL (default: port `5433`, database `store_db`)
- Maven 3.9+
- A Stripe account (for payment features)

### 1. Clone & Configure

```bash
git clone <your-repo-url>
cd store
```

Create a `.env` file in the project root (loaded automatically via `spring-dotenv`):

```env
JWT_TOKEN_SECRET=your-256-bit-secret
JWT_TOKEN_EXPIRATION=300
JWT_REFRESH_EXPIRATION=604800
STRIPE_SECRET_KEY=sk_test_...
STRIPE_WEBHOOK_SCRET_KEY=whsec_...
```

### 2. Set Up the Database

```sql
CREATE DATABASE store_db;
```

Liquibase will run all migrations automatically on startup.

### 3. Build & Run

```bash
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080`.

### 4. Explore the API

Open `http://localhost:8080/swagger-ui.html` for the interactive Swagger UI.

### Stripe Webhook (Local Development)

Use the Stripe CLI to forward webhook events to your local server:

```bash
stripe listen --forward-to localhost:8080/checkout/webhook
```

---

## Environment Variables

| Variable | Description | Default |
|---|---|---|
| `JWT_TOKEN_SECRET` | HMAC secret key for signing JWTs | **Required** |
| `JWT_TOKEN_EXPIRATION` | Access token TTL in seconds | `300` (5 min) |
| `JWT_REFRESH_EXPIRATION` | Refresh token TTL in seconds | `604800` (7 days) |
| `STRIPE_SECRET_KEY` | Stripe secret API key | **Required** |
| `STRIPE_WEBHOOK_SCRET_KEY` | Stripe webhook signing secret | **Required** |

---

## Project Structure

```
store/
├── src/
│   ├── main/
│   │   ├── java/com/leroy/store/
│   │   │   ├── config/           # JwtConfig, SecurityConfig
│   │   │   ├── controllers/      # REST controllers
│   │   │   ├── dtos/             # Request & response DTOs
│   │   │   ├── entities/         # JPA entities & enums
│   │   │   ├── exceptions/       # Custom exceptions, GlobalExceptionHandler
│   │   │   ├── filters/          # JwtAuthenticationFilter, LoggingFilter
│   │   │   ├── mappers/          # MapStruct interfaces
│   │   │   ├── payment/          # Stripe integration & CheckoutService
│   │   │   ├── repositories/     # Spring Data JPA repositories
│   │   │   ├── services/         # Business logic layer
│   │   │   └── validation/       # @Lowercase custom constraint
│   │   └── resources/
│   │       ├── application.yaml
│   │       └── db/
│   │           ├── changelog/    # Liquibase YAML changelogs (001–005)
│   │           └── script/       # Raw SQL migration scripts
│   └── test/
│       ├── data/products.json    # Test seed data
│       └── java/.../StoreApplicationTests.java
└── pom.xml
```