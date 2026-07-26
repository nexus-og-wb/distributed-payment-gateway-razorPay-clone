<div align="center">

# 💳 Distributed Payment Gateway
### *A Razorpay-Inspired Backend, Built From Scratch*

*A backend-first simulation of how modern payment gateways process transactions — designed around clean architecture, business-domain-oriented modules, and patterns that scale toward a distributed, event-driven system.*

![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Status](https://img.shields.io/badge/Status-Work%20In%20Progress-FFC107?style=for-the-badge)

</div>

---

> ⚠️ **Work in Progress.** This project is under active development. Every section below distinguishes clearly between what's **implemented (✅)**, **in progress (🚧)**, and **planned (⏳)**. Nothing marked planned exists in the codebase yet — treat this README as a living document, not a finished spec sheet.

## Table of Contents

- [Project Overview](#project-overview)
- [Motivation](#motivation)
- [Features](#features)
- [Current Progress](#current-progress)
- [Technology Stack](#technology-stack)
- [Architecture Overview](#architecture-overview)
- [Architecture Principles](#architecture-principles)
- [High-Level Flow Diagram](#high-level-flow-diagram)
- [Package Structure](#package-structure)
- [Design Patterns Used](#design-patterns-used)
- [Current Modules](#current-modules)
- [Planned Modules](#planned-modules)
- [How to Run Locally](#how-to-run-locally)
- [Environment Variables](#environment-variables)
- [API Overview](#api-overview)
- [Future Roadmap](#future-roadmap)
- [Learning Objectives](#learning-objectives)
- [Contributing](#contributing)
- [Contact](#contact)

---

## Project Overview

**Distributed Payment Gateway** is a backend system built from the ground up in Java and Spring Boot, modeled on how real-world payment gateways such as Razorpay process transactions internally. It is **not** a CRUD demo — the project is organized around clean architecture, business-domain-oriented package boundaries, and patterns intended to scale toward a distributed, event-driven system (Kafka, Redis, Kubernetes, Saga-based transactions) as it matures.

The system currently focuses on the core transaction path — merchant onboarding, order creation, and payment initiation — routed through a pluggable strategy/adapter layer built to support multiple payment methods (UPI, Card, Net Banking) and, eventually, multiple real banking integrations behind one consistent interface.

*This is an independent, educational project inspired by publicly known payment-gateway architecture patterns. It is not affiliated with, endorsed by, or connected to Razorpay Software Pvt. Ltd. Its purpose is to practice production-style backend architecture and distributed-systems concepts — it is not an attempt to replicate every feature of Razorpay.*

## Motivation

Real banking and payment-gateway APIs are not publicly available to students or individual developers — production integrations require merchant agreements, regulatory compliance, and formal banking partnerships that are out of reach for a personal project. This project exists to close that gap by building the *internals* of a payment gateway from first principles: order lifecycle management, a pluggable payment-processing pipeline, and a **Mock Banking System** that simulates realistic banking behavior, covering:

- Account validation
- Balance validation
- Authorization
- Insufficient balance
- Bank timeout
- Bank unavailable
- Random latency
- Transaction records

The payment layer communicates with the bank exclusively through the adapter layer (see [Architecture Overview](#architecture-overview)), so the mock implementation can later be replaced with a real banking integration without changing business logic in the processor or service layers.

The broader goal is to practice the architectural patterns production fintech systems rely on: clean, domain-oriented module boundaries; SOLID principles; Strategy/Adapter/Router patterns for extensibility; and, as the project grows, event-driven communication and distributed transaction management across independently deployable services.

Java 25 (LTS) was chosen specifically for its long support window and modern language features (records, pattern matching, virtual threads) that suit high-concurrency, I/O-heavy payment workloads well.

## Features

**✅ Implemented**
- Merchant registration and onboarding
- Merchant authentication
- API key issuance and management
- Order creation and tracking
- Payment initiation
- Payment Gateway Router — dispatches requests to the correct payment-method strategy
- Strategy Pattern scaffolding for payment-method routing (UPI, Card, Net Banking) — selects the correct strategy per request; the execution logic itself lives in the Payment Processor, which is still in progress (see below)
- Adapter Pattern abstraction for gateway/bank integration (interfaces defined; no live integration yet)
- Payment persistence with state-transition tracking (`PaymentTransitionLog`)

**🚧 In Progress**
- Payment Processor — the execution layer that carries a payment through to a result
- Mock Banking System — simulates account validation, balance validation, authorization, insufficient balance, bank timeout, bank unavailable, random latency, and transaction records

**⏳ Planned**
- Refund processing
- Settlement engine
- Webhook delivery for merchant notifications
- Event-driven communication via Apache Kafka
- Distributed transactions via the Saga pattern
- Redis caching / distributed locking
- API Gateway as a single entry point
- Containerization (Docker) and orchestration (Kubernetes)
- Spring Security + JWT-based authentication

## Current Progress

| Area | Status |
|---|---|
| Merchant Registration | ✅ Completed |
| Merchant Authentication | ✅ Completed |
| API Key Management | ✅ Completed |
| Order Creation | ✅ Completed |
| Payment Initiation | ✅ Completed |
| Payment Gateway Router | ✅ Completed |
| Strategy Pattern | ✅ Completed |
| Adapter Pattern | ✅ Completed |
| Payment Persistence | ✅ Completed |
| Payment Processor | 🚧 In Progress |
| Mock Banking System | 🚧 In Progress |
| Refund Service | ⏳ Planned |
| Settlement Engine | ⏳ Planned |
| Webhook Service | ⏳ Planned |
| Kafka Integration | ⏳ Planned |
| Saga Pattern | ⏳ Planned |
| Notification Service | ⏳ Planned |
| Redis | ⏳ Planned |
| API Gateway | ⏳ Planned |
| Docker | ⏳ Planned |
| Kubernetes | ⏳ Planned |

## Technology Stack

**Current Technologies**

| Category | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot |
| Persistence | Spring Data JPA, Hibernate |
| Database | PostgreSQL |
| Object Mapping | MapStruct |
| Boilerplate Reduction | Lombok |
| Build Tool | Maven |

**Planned Technologies**

| Category | Technology | Purpose |
|---|---|---|
| Messaging | Apache Kafka | Event-driven communication between modules |
| Transactions | Saga Pattern | Managing distributed transactions |
| Caching | Redis | Caching, distributed locks |
| Security | Spring Security + JWT | Authentication & authorization |

**Future Technologies**

| Category | Technology | Purpose |
|---|---|---|
| Containerization | Docker | Packaging services for consistent environments |
| Orchestration | Kubernetes | Deploying and scaling distributed services |
| Gateway | API Gateway | Single entry point once decomposed into microservices |

## Architecture Overview

The project is currently a **Modular Monolith** — a single deployable application, internally organized into cohesive, domain-oriented modules with clear boundaries between them. It avoids a traditional **package-by-layer** structure (top-level `controllers/`, `services/`, `repositories/`); instead, it uses a **package-by-business-domain** structure, where each module is a vertical slice that owns its full stack:

```
merchant/   -> controller, service, repository, entity, dto, mapper
payment/    -> controller, service, repository, entity, dto, mapper (+ gateway/processor/router/adapter/config)
common/     -> shared building blocks used across all domains
```

This structure is intentional preparation for a future migration to **Microservices**: module boundaries are drawn so each one can eventually be extracted into an independently deployable service without a fundamental redesign. That decomposition is a planned future step, not the current state (see [Future Roadmap](#future-roadmap), Phase 11).

**Inside the `payment` module**, payment processing is not implemented as a single `PaymentService`. It is split across dedicated packages:

- **`router/`** — `PaymentGatewayRouter` inspects an incoming payment request and picks the correct processor for the requested payment method.
- **`processor/`** *(🚧 in progress)* — one implementation per payment method (UPI, Card, Net Banking), following the **Strategy Pattern** so new payment methods can be added without touching existing code.
- **`adapter/`** — normalizes the interface between the processor layer and whatever sits on the other side (currently a Mock Bank under development; eventually, real banking/gateway APIs), following the **Adapter Pattern**.
- **`gateway/`** — gateway-level orchestration tying router, processor, and adapter together.
- **`config/`** — Spring configuration specific to the payment module.

This separation is what will let the Mock Banking System be replaced with a real bank integration later without changing business logic in the processor or service layers.

## Architecture Principles

The codebase is guided by a consistent set of principles rather than being organized ad hoc:

- **SOLID Principles** — applied across the service and domain layers
- **Clean Code** — intention-revealing naming and small, focused classes and methods
- **Layered Architecture inside each module** — each domain module (`merchant/`, `payment/`) internally separates controller, service, repository, entity, DTO, and mapper responsibilities
- **Domain-oriented package organization** — packages are structured by business domain rather than by technical layer (see [Architecture Overview](#architecture-overview))
- **Design Pattern based implementation** — Strategy, Adapter, Router, Builder, Repository, and Mapper patterns are used where they solve a real problem (see [Design Patterns Used](#design-patterns-used))
- **Separation of Concerns** — routing, processing, and gateway/adapter logic are kept in distinct packages within the payment module instead of one large service class
- **Extensibility** — new payment methods, processors, or gateway integrations can be added without modifying existing code, by design of the Strategy/Adapter/Router layering

## High-Level Flow Diagram

```
 Merchant App
      |
      |  1. Create Order
      v
 Order Service
      |
      |  2. Initiate Payment
      v
 Payment Gateway Router
      |
      |  3. Select Strategy: UPI | Card | Net Banking
      v
 Payment Processor        [in progress]
      |
      |  4. Delegate via Adapter
      v
 Payment Gateway Adapter
      |
      |  5. Communicate with Bank
      v
 Mock Bank (simulated)    [in progress]
      |
      |  6. Return Payment Result
      v
 Persist Payment  -->  Return Response
```

## Package Structure

```
src/main/java/.../paymentgateway
|
+-- common/
|     +-- entity/       (BaseEntity)
|     +-- vo/           (Money)
|     +-- enums/        (BusinessType, Environment, MerchantStatus,
|     |                  OrderStatus, PaymentStatus, RefundStatus,
|     |                  SettlementStatus, WebhookEventStatus,
|     |                  PaymentMethod, PaymentActor, PaymentEvent, UserRole)
|     +-- exception/    (global exception handling)
|     +-- util/         (shared utility classes)
|
+-- merchant/
|     +-- entity/       (Merchant, AppUser, Customer)
|     +-- controller/
|     +-- service/
|     +-- repository/
|     +-- dto/
|     +-- mapper/
|
+-- payment/
|     +-- entity/       (Order, Payment, Refund, PaymentTransitionLog)
|     +-- controller/
|     +-- service/
|     +-- repository/
|     +-- dto/
|     +-- mapper/
|     +-- gateway/
|     +-- processor/    [in progress]
|     +-- router/       (PaymentGatewayRouter)
|     +-- adapter/
|     +-- config/
|
+-- operations/         [planned - scope TBD]
|
+-- vault/              [planned - secure data storage]
```

> Adjust the base package (`.../paymentgateway`) to match your actual groupId/artifactId.

## Design Patterns Used

| Pattern | Purpose | Status |
|---|---|---|
| Strategy Pattern | Interchangeable payment-method processors (UPI, Card, Net Banking) | ✅ |
| Adapter Pattern | Normalizes integration with different gateway/bank interfaces | ✅ |
| Router Pattern | Dispatches incoming payment requests to the correct processor | ✅ |
| Builder Pattern | Constructing complex domain objects | ✅ |
| Repository Pattern | Abstracts persistence logic per module | ✅ |
| Mapper Pattern (MapStruct) | Entity ↔ DTO conversion | ✅ |
| Dependency Injection | Spring-managed component wiring throughout | ✅ |
| Factory Pattern | Object creation, where appropriate | ⏳ |
| Saga Pattern | Distributed transaction management across future microservices | ⏳ |

## Current Modules

### `common/`
- `BaseEntity` — shared base class for JPA entities
- `Money` — a value object encapsulating amount + currency, avoiding the floating-point and currency-mixing bugs common in naive payment implementations
- **Enums:** `BusinessType`, `Environment`, `MerchantStatus`, `OrderStatus`, `PaymentStatus`, `RefundStatus`, `SettlementStatus`, `WebhookEventStatus`, `PaymentMethod`, `PaymentActor`, `PaymentEvent`, `UserRole`
- Global exception handling
- Shared utility classes

### `merchant/`
- **Domain entities:** `Merchant`, `AppUser`, `Customer`
- **Capabilities:** merchant registration, merchant authentication, API key management, merchant webhook configuration
- **Supporting layers:** repository, service, controller, DTOs, MapStruct mapper

### `payment/`
- **Domain entities:** `Order`, `Payment`, `Refund`, `PaymentTransitionLog` (state-transition audit trail)
- **Supporting layers:** repository, service, controller, DTOs, mapper
- **Processing sub-packages:** `gateway/`, `processor/` 🚧, `router/` (`PaymentGatewayRouter`), `adapter/`, `config/`

## Planned Modules

The following exist in the package structure but don't yet have implementation behind them (or are only partially scaffolded):

### `operations/`
Reserved for internal operational tooling — scope not yet finalized.

### `vault/`
Reserved for secure handling of sensitive payment data. In payment-gateway systems, a module like this typically handles tokenization and secure storage of card/account details so sensitive data never touches core business logic directly — that's the intended direction here, but nothing in this module is implemented yet.

### Future Services (post-decomposition)
As the project moves toward a distributed architecture, these independent services are planned:

- Merchant Service
- Payment Service
- Refund Service
- Settlement Service
- Webhook Service
- Notification Service

...coordinated through an **API Gateway**, communicating over an **Apache Kafka** event bus, backed by **Redis** caching, and using the **Saga Pattern** for distributed transaction management.

## How to Run Locally

### Prerequisites
- JDK 25
- Maven 3.9+
- PostgreSQL 14+

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/<your-username>/distributed-payment-gateway.git
cd distributed-payment-gateway

# 2. Create a PostgreSQL database
createdb payment_gateway_db

# 3. Configure environment variables (see below),
#    or edit src/main/resources/application.yaml directly

# 4. Build the project
mvn clean install

# 5. Run the application
mvn spring-boot:run
```

The application starts on `http://localhost:8080` by default (configurable via `SERVER_PORT`).

## Environment Variables

| Variable | Description | Example |
|---|---|---|
| `DB_URL` | PostgreSQL JDBC connection string | `jdbc:postgresql://localhost:5432/payment_gateway_db` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `postgres` |
| `SERVER_PORT` | Port the application listens on | `8080` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` |

Example `application.yml`:

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

server:
  port: ${SERVER_PORT:8080}
```

> These are illustrative — adjust to match your actual configuration.

## API Overview

| Module | Capability (Implemented) | Notes |
|---|---|---|
| Merchant | Registration, authentication, API key issuance & management | |
| Order | Order creation, retrieval | |
| Payment | Payment initiation, routing to processor, persistence | Processor execution still 🚧 |

Formal REST contracts, request/response DTO schemas, and interactive API documentation (springdoc-openapi / Swagger UI) are planned as the controller layer stabilizes — see [Future Roadmap](#future-roadmap).

## Future Roadmap

**Phase 1 — Complete Payment Processing**
- Finish the Payment Processor implementation for all supported payment methods (UPI, Card, Net Banking)

**Phase 2 — Mock Banking System**
- Simulate account validation, balance validation, authorization, insufficient balance, bank timeout, bank unavailable, random latency, and transaction records

**Phase 3 — Refunds**
- Implement the Refund Service on top of the existing `Refund` entity

**Phase 4 — Settlement**
- Build the Settlement Engine for merchant payouts

**Phase 5 — Webhooks**
- Implement the Webhook Service for merchant event notifications

**Phase 6 — Kafka + Event-Driven Architecture**
- Introduce Apache Kafka for asynchronous, event-driven communication between modules

**Phase 7 — Saga Pattern**
- Implement the Saga Pattern for distributed transaction management

**Phase 8 — Redis**
- Introduce Redis for caching and distributed locking

**Phase 9 — Docker**
- Containerize the application with Docker

**Phase 10 — Kubernetes**
- Orchestrate deployment with Kubernetes

**Phase 11 — Split the Modular Monolith into Microservices**
- Decompose the modular monolith into independently deployable services (Merchant, Payment, Refund, Settlement, Webhook, Notification), introducing an API Gateway as the single entry point and Spring Security + JWT for cross-service authentication, communicating over a Kafka event bus

## Learning Objectives

This project exists to practice:

- Applying Clean Architecture and SOLID principles in a real, non-trivial backend system
- Structuring a codebase using a business-domain-oriented package structure rather than by technical layer
- Implementing extensible payment processing using Strategy, Adapter, and Router patterns
- Designing an event-driven architecture and reasoning about eventual consistency
- Understanding distributed-systems failure modes (timeouts, partial failures, retries) through a realistic Mock Banking System
- Practicing distributed transaction management with the Saga pattern
- Evolving a well-bounded modular monolith toward a microservices architecture
- Applying enterprise coding standards: layered validation, global exception handling, consistent DTO/mapper boundaries

## Contributing

This is primarily a personal learning project, but constructive feedback, issue reports, and suggestions are always welcome.

If you'd like to contribute once the codebase stabilizes:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes with clear messages
4. Push to your fork and open a Pull Request

Please open an issue first for larger changes, since the architecture is still evolving.

## Contact

> Mail:- prashantsingh4007@gmail.com

---

<div align="center">

*This README evolves alongside the codebase — always cross-check the [Current Progress](#current-progress) table for the latest implementation status.*

</div>