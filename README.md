# RazorPay Distributed Payment Gateway

[![Java 25](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/projects/jdk/25/)
[![Spring Boot 4.1.x](https://img.shields.io/badge/Spring%20Boot-4.1.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.3-blue.svg)](https://spring.io/projects/spring-cloud)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-Distributed%20Events-black.svg)](https://kafka.apache.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16%20(Per--Service)-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-Distributed%20Cache%20%26%20RateLimit-red.svg)](https://redis.io/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-Kind%20Cluster-326CE5.svg)](https://kubernetes.io/)
[![Prometheus](https://img.shields.io/badge/Prometheus-Metrics-E6522C.svg)](https://prometheus.io/)
[![Grafana](https://img.shields.io/badge/Grafana-Monitoring-F46800.svg)](https://grafana.com/)
[![Zipkin](https://img.shields.io/badge/Zipkin-Distributed%20Tracing-orange.svg)](https://zipkin.io/)

A distributed, event-driven payment gateway architecture inspired by Razorpay, built as an exploration of real-world distributed systems engineering. The platform models payment orchestration, asynchronous event propagation, decoupled settlement batches, isolated card tokenization, and cryptographically verified webhook delivery across microservices.

**Repository:** [https://github.com/nexus-og-wb/distributed-payment-gateway-razorPay-clone](https://github.com/nexus-og-wb/distributed-payment-gateway-razorPay-clone)  
**Author:** Prashant Singh  
**Project Status:** Completed portfolio implementation demonstrating distributed payment processing, event-driven architecture, reliability patterns, Kubernetes deployment, observability, and performance optimization.

---

## Engineering Highlights

- **Distributed Microservice Architecture**: Core payment processing decomposed across domain-aligned Spring Boot microservices (`merchant-service`, `payment-service`, `vault-service`, `operations-service`), edge routing via `api-gateway-service`, and platform services (`config-service`, `discovery-service`).
- **Database-per-Service**: Independent PostgreSQL schemas per microservice enforcing strict domain boundaries with zero cross-service schema contention or foreign-key coupling.
- **Transactional Outbox Pattern**: Atomic local database transactions persisting payment entity updates and outbox events together, asynchronously polled via `FOR UPDATE SKIP LOCKED` and published to Apache Kafka.
- **Multi-Layer Idempotency**: Redis-backed fast-path idempotency caching (24h TTL) paired with PostgreSQL unique constraints on `(merchant_id, idempotency_key)` to guarantee exactly-once payment semantics under network retries.
- **Deterministic Payment State Machine**: Strict transition validation guarding all lifecycle states (`CREATED` ? `AUTHORIZING` ? `AUTHORIZED` ? `CAPTURING` ? `CAPTURED` ? `SETTLED` / `REFUNDED`) with append-only transition audit logs.
- **Dual-Scheme Authentication & Edge Gateway**: Spring Cloud Gateway WebMVC edge reverse proxy enforcing JWT signature verification for dashboard users and HTTP Basic API Key authentication with key rotation grace periods for server-to-server traffic.
- **Card Data Vault Isolation**: Dedicated `vault-service` storing primary account numbers (PAN) encrypted at rest using AES-256 GCM, issuing non-sensitive UUID tokens for payment orchestration.
- **Reliable Webhook Delivery Engine**: Asynchronous webhook dispatch with HMAC-SHA256 signature verification, exponential backoff across 7 attempts, and Dead-Letter Queue (DLQ) auditing.
- **Kubernetes Deployment (Kind / Kustomize)**: Declarative multi-service deployment orchestrating edge gateways, business microservices, and stateful infrastructure (PostgreSQL, Redis, Kafka) using Kustomize overlays.
- **Full-Stack Observability**: Production-grade telemetry pipeline combining Spring Boot Actuator, Prometheus metric scraping, custom Grafana dashboards (JVM memory & process CPU), and Zipkin distributed tracing across RPC boundaries.
- **Empirical Bottleneck-Driven Optimization**: Identified gateway CPU saturation caused by repetitive BCrypt hashing under concurrent load; engineered a 30-second Redis API-key verification cache that reduced measured P99 latency by **~86.1%** (from **~16.000s** down to **~2.221s**) in local benchmark testing.

---

## Table of Contents

- [Engineering Highlights](#engineering-highlights)
- [Overview](#overview)
- [Project Evolution](#project-evolution)
- [Architecture](#architecture)
  - [High-Level System Architecture](#high-level-system-architecture)
  - [Service-to-Service Communication](#service-to-service-communication)
  - [Event-Driven Kafka Architecture](#event-driven-kafka-architecture)
  - [Deployment Architecture (Kubernetes / Kind)](#deployment-architecture-kubernetes--kind)
  - [Observability Architecture](#observability-architecture)
- [Services](#services)
  - [Service Inventory](#service-inventory)
  - [API Gateway (`api-gateway-service`)](#api-gateway-api-gateway-service)
  - [Merchant Service (`merchant-service`)](#merchant-service-merchant-service)
  - [Payment Service (`payment-service`)](#payment-service-payment-service)
  - [Vault Service (`vault-service`)](#vault-service-vault-service)
  - [Operations Service (`operations-service`)](#operations-service-operations-service)
  - [Config Service (`config-service`)](#config-service-config-service)
  - [Discovery Service (`discovery-service`)](#discovery-service-discovery-service)
  - [Common Library (`common-lib`)](#common-library-common-lib)
- [Payment Lifecycle](#payment-lifecycle)
- [Authentication & Authorization](#authentication--authorization)
- [Rate Limiting](#rate-limiting)
- [Idempotency](#idempotency)
- [Payment State Machine](#payment-state-machine)
- [Kafka & Event-Driven Architecture](#kafka--event-driven-architecture)
- [Transaction Management & Consistency](#transaction-management--consistency)
- [Webhooks](#webhooks)
- [Settlement](#settlement)
- [Vault & Sensitive Data](#vault--sensitive-data)
- [Database Architecture](#database-architecture)
- [Redis Architecture](#redis-architecture)
- [Resilience](#resilience)
- [Distributed Scheduling](#distributed-scheduling)
- [Kubernetes Deployment](#kubernetes-deployment)
- [Observability](#observability)
- [Performance & Load Testing](#performance--load-testing)
- [Performance Optimization Case Study](#performance-optimization-case-study)
  - [BCrypt Authentication Bottleneck & 86% Latency Reduction](#bcrypt-authentication-bottleneck--86-latency-reduction)
  - [External Network Call Extraction from Database Transactions](#external-network-call-extraction-from-database-transactions)
- [Resource & Capacity Planning](#resource--capacity-planning)
- [Project Structure](#project-structure)
- [Technology Stack](#technology-stack)
- [Local Development](#local-development)
- [Kubernetes Setup (Kind)](#kubernetes-setup-kind)
- [API Documentation](#api-documentation)
- [Security Considerations](#security-considerations)
- [Known Limitations](#known-limitations)
- [Engineering Tradeoffs](#engineering-tradeoffs)
- [What This Project Demonstrates](#what-this-project-demonstrates)
- [Future Improvements](#future-improvements)
- [Scaling the Architecture to 10,000 TPS](#scaling-the-architecture-to-10000-tps)
  - [10,000 TPS Traffic Model](#10000-tps-traffic-model)
  - [Horizontal Scaling Strategy](#horizontal-scaling-strategy)
  - [Database Scaling (PostgreSQL)](#database-scaling-postgresql)
  - [Database-per-Service at Scale](#database-per-service-at-scale)
  - [Database Connection Management & Pooling](#database-connection-management--pooling)
  - [Redis Cluster Scaling](#redis-cluster-scaling)
  - [Kafka & Event Streaming at Scale](#kafka--event-streaming-at-scale)
  - [API Gateway Scaling & Rate Limiting](#api-gateway-scaling--rate-limiting)
  - [Rate Limiting at Scale](#rate-limiting-at-scale)
  - [Failure Isolation at 10,000 TPS](#failure-isolation-at-10000-tps)
  - [Kubernetes Autoscaling (HPA)](#kubernetes-autoscaling-hpa)
  - [Observability at Scale](#observability-at-scale)
  - [Illustrative 10,000 TPS Capacity Plan](#illustrative-10000-tps-capacity-plan)
  - [Architecture Comparison: Current vs. 10k TPS Target](#architecture-comparison-current-vs-10k-tps-target)
  - [Infrastructure Cost Considerations](#infrastructure-cost-considerations)
  - [Target 10,000 TPS System Architecture Diagram](#target-10000-tps-system-architecture-diagram)
- [Final Project Status](#final-project-status)

---

## Overview

### What the System Is
This project is an event-driven payment processing platform that models the core transactional lifecycle of a commercial payment gateway such as Razorpay. It coordinates order creation, payment authorization, automated capture, payment refunding, merchant settlement, and webhook notification delivery across isolated microservices.

### The Problem It Models
Payment gateways represent one of the most challenging problem spaces in distributed computing:
1. **Reliable State and Event Persistence**: Critical payment state changes and corresponding outbox events must be committed atomically within the owning database transaction. Dropping an authorization confirmation or settlement credit directly results in financial discrepancies.
2. **Strict Consistency vs. High Availability**: Orders and payments require atomic transactional guarantees locally, yet must coordinate across asynchronous boundaries without distributed locking (two-phase commit).
3. **Dual-Write Hazards**: Persisting state to a relational database while simultaneously emitting notifications to message brokers invariably encounters partial failures unless mediated by patterns like the Transactional Outbox.
4. **Idempotency Under Concurrent Retries**: Unstable network connections frequently cause clients to retry mutating requests, creating severe risks of duplicate payment processing.
5. **Rate & Abuse Governance**: High-throughput public API endpoints demand low-latency rate limiting and credential authentication without saturating compute resources.

### Scope & Non-Goals
This project is an **architectural simulation and engineering study**. It connects to mock payment network adapters (simulated bank callback handlers and card processing simulators) rather than live financial settlement networks (such as Visa/Mastercard host networks or NPCI UPI rails). It does **not** claim PCI-DSS certification.

---

## Project Evolution

The codebase was constructed through deliberate evolutionary phases to explore the real-world trade-offs of microservice decomposition:

```mermaid
flowchart TD
    M1[1. Modular Monolith] -->|Domain separation, shared schema contention| M2[2. Domain-Oriented Modules]
    M2 -->|Independent deployability, failure isolation| M3[3. Independent Microservices]
    M3 -->|Elimination of synchronous coupling & dual writes| M4[4. Event-Driven Architecture with Kafka & Outbox]
    M4 -->|Containerization, declarative orchestration| M5[5. Distributed Deployment on Kubernetes / Kind]
    M5 -->|End-to-end tracing, metrics scraping| M6[6. Observability via Prometheus, Grafana & Zipkin]
    M6 -->|Load testing, bottleneck identification| M7[7. Performance Optimization & Cache Tuning]
```

### Why the Architecture Evolved
- **From Monolith to Microservices**: A centralized codebase simplifies transactions but couples release cycles. For example, updating the asynchronous webhook dispatch logic or card tokenization schemes should never risk destabilizing core order creation and payment authorization flows. Decomposing into `merchant-service`, `payment-service`, `vault-service`, and `operations-service` achieved decoupled deployment boundaries.
- **From Synchronous REST to Kafka Event Streaming**: In early designs, the payment service invoked webhook delivery and settlement synchronously or via direct REST endpoints. If the downstream merchant endpoints or operations database stalled, payment confirmation calls degraded. Introducing Apache Kafka and the **Transactional Outbox Pattern** fully decoupled payment success from downstream operational processing.
- **From Simple Authentication to Verification Caching**: As demonstrated during load testing, microservice edge gateways running standard CPU-bound BCrypt password-hash checks for every incoming request saturate CPU capacity. The introduction of Redis as an ephemeral verification cache eliminated redundant hashing, highlighting how real-world distributed architectures evolve under empirical profiling.

---

## Architecture

### High-Level System Architecture

```mermaid
flowchart TB
    subgraph Clients ["Client Layer"]
        B_MERCHANT["Merchant Web Dashboard / Admin"]
        B_CUSTOMER["Customer Checkout / Browser"]
        S2S["Merchant Backend (Server-to-Server)"]
    end

    subgraph Edge ["Edge Ingress Layer (Port 8080)"]
        GW["api-gateway-service\n(Spring Cloud Gateway WebMVC)"]
        AUTH["GatewayAuthFilter\nJWT Verification | API-Key Auth"]
        RL["Distributed Rate Limiter\n(Redis Fixed/Sliding Window)"]
        CACHE["API-Key Secret Cache\n(Redis 30s TTL)"]
    end

    subgraph Core ["Core Microservice Layer"]
        MS["merchant-service (Port 9010)\nMerchant Onboarding, API Keys,\nCustomer Registry, Webhook Config"]
        PS["payment-service (Port 9020)\nOrders, Payments, Refunds,\nPayment State Machine, Outbox Poller"]
        VS["vault-service (Port 9040)\nCard Tokenization, AES-256 Encryption,\nCard Data Vault Isolation"]
        OS["operations-service (Port 9030)\nSettlements, Webhook Delivery,\nRetry Queue, DLQ Recorder"]
    end

    subgraph Persistence ["Database-Per-Service (PostgreSQL 16)"]
        DB_M[("merchant_db\n(Port 5432)")]
        DB_P[("payment_db\n(Port 5432)")]
        DB_V[("vault_db\n(Port 5432)")]
        DB_O[("operations_db\n(Port 5432)")]
    end

    subgraph Messaging ["Event Streaming Layer"]
        KAFKA{{"Apache Kafka Broker\nTopics: payments.events, orders.events,\nrefunds.events, settlements.events"}}
    end

    subgraph DistributedCache ["Shared Coordination Layer"]
        REDIS[("Redis Cluster / Standalone\n• Idempotency Keys\n• API Key Cache\n• Rate Limit Windows")]
    end

    subgraph External ["External Integrations"]
        BANK_SIM["Bank / Network Simulators\n(Card, UPI, NetBanking)"]
        MERCHANT_WH["Merchant Webhook URL\n(HTTP POST HMAC-Signed)"]
    end

    B_MERCHANT -->|Bearer JWT| GW
    B_CUSTOMER -->|Public Order Session| GW
    S2S -->|Basic API-Key:Secret| GW

    GW --> AUTH
    AUTH <--> CACHE
    AUTH <--> RL
    RL <--> REDIS

    GW -->|Forward: /api/v1/merchants/**| MS
    GW -->|Forward: /api/v1/orders/**, /payments/**| PS
    GW -->|Forward: /api/v1/vault/**| VS
    GW -->|Forward: /api/v1/operations/**| OS

    MS <--> DB_M
    PS <--> DB_P
    VS <--> DB_V
    OS <--> DB_O

    PS -->|Charge Delegation (Internal Feign)| VS
    PS -->|Customer Lookup (Internal Feign)| MS
    PS -->|Simulated Authorization/Capture| BANK_SIM

    PS -- Outbox Poller (Atomic DB Read) --> KAFKA
    KAFKA -->|Consumer: Webhook & Settlement Groups| OS
    OS -->|Signed Webhook Dispatch| MERCHANT_WH
```

### Service-to-Service Communication

```mermaid
flowchart LR
    subgraph Edge
        GW[api-gateway-service]
    end

    subgraph SynchronousFeign ["Synchronous Communication (OpenFeign + HTTP/2 HC5)"]
        GW -->|Route Dispatch| MS[merchant-service]
        GW -->|Route Dispatch| PS[payment-service]
        GW -->|Route Dispatch| VS[vault-service]
        GW -->|Route Dispatch| OS[operations-service]
        
        GW -.->|ApiKeyLookupClient| MS
        PS -.->|CustomerServiceClient| MS
        PS -.->|VaultServiceClient| VS
        OS -.->|MerchantServiceClient| MS
        OS -.->|PaymentServiceClient| PS
    end

    subgraph AsynchronousKafka ["Asynchronous Communication (Apache Kafka)"]
        PS -- Outbox Poller --> KF[Kafka Topics]
        KF --> OS
    end
```

### Event-Driven Kafka Architecture

```mermaid
flowchart TD
    subgraph PaymentService ["payment-service Domain"]
        TX[Payment State Change Transaction]
        DB[(payment_db)]
        OB[outbox_event Table]
        POLLER[OutboxPoller\nScheduled 500ms]
    end

    subgraph KafkaBrokers ["Kafka Topics (Partition Key: merchantId)"]
        TOPIC_PAY["payments.events"]
        TOPIC_ORD["orders.events"]
        TOPIC_REF["refunds.events"]
        TOPIC_SET["settlements.events"]
    end

    subgraph OperationsService ["operations-service Domain"]
        WH_CONS["WebhookKafkaConsumer\nGroup: operations-webhook-group"]
        SET_ENG["SettlementEngine\nGroup: operations-settlement-group"]
        RETRY_Q["WebhookRetryQueue\n(In-Memory PriorityQueue)"]
        DLQ_REC["WebhookDlqRecorder\n(dlq_event Table)"]
    end

    TX -->|1. Commit Payment & Outbox Event Atomically| DB
    DB --- OB
    POLLER -->|2. Poll PENDING Events SKIP LOCKED| OB
    POLLER -->|3. Publish Event Record| TOPIC_PAY & TOPIC_ORD & TOPIC_REF
    POLLER -->|4. Update status = PUBLISHED| OB

    TOPIC_PAY & TOPIC_ORD & TOPIC_REF & TOPIC_SET --> WH_CONS
    TOPIC_PAY --> SET_ENG

    WH_CONS -->|Enqueue Delivery| RETRY_Q
    WH_CONS -.->|Logic / Deserialization Failure| DLQ_REC
```

### Deployment Architecture (Kubernetes / Kind)

```mermaid
flowchart TB
    subgraph K8s ["Kubernetes Cluster (Namespace: razorpay-core)"]
        subgraph Ingress ["Ingress & Routing"]
            GW_POD["api-gateway\nService: NodePort 30080\nLimits: 4 CPU / 6Gi"]
        end

        subgraph BusinessPods ["Microservice Deployments"]
            MS_POD["merchant-service\nLimits: 4 CPU / 6Gi"]
            PS_POD["payment-service\nLimits: 4 CPU / 8Gi"]
            OS_POD["operations-service\nLimits: 1 CPU / 2Gi"]
            VS_POD["vault-service\nLimits: 1 CPU / 2Gi"]
        end

        subgraph InfraPods ["Infrastructure Deployments & StatefulSets"]
            CONF_POD["config-service\nLimits: 500m / 768Mi"]
            PG_POD[("postgres:16\nmax_connections: 300\nLimits: 1 CPU / 1536Mi")]
            RD_POD[("redis:7-alpine\nStateful Service")]
            KF_POD{{"kafka (KRaft)\nBitnami Kafka 3.7"}}
            KF_UI["kafka-ui\nPort 8090"]
        end

        subgraph ObservabilityPods ["Monitoring & Tracing"]
            PROM_POD["prometheus:v2.51.0\nPort 9090"]
            GRAF_POD["grafana:10.4.0\nPort 3000"]
            ZIP_POD["zipkin:latest\nPort 9411"]
        end
    end

    GW_POD --> MS_POD & PS_POD & OS_POD & VS_POD
    BusinessPods <--> PG_POD & RD_POD & KF_POD
    BusinessPods -.-> CONF_POD
    PROM_POD -->|Scrape Actuator /metrics| BusinessPods & GW_POD
    GRAF_POD --> PROM_POD
    BusinessPods -->|Send Traces| ZIP_POD
```

### Observability Architecture

```mermaid
flowchart LR
    subgraph Services ["Monitored Components"]
        GW[api-gateway]
        MS[merchant-service]
        PS[payment-service]
        OS[operations-service]
        VS[vault-service]
    end

    subgraph MetricsFlow ["Metrics Pipeline"]
        PROM[Prometheus Server\nScrape Interval: 10s]
        GRAF[Grafana Dashboards\n• Service Resources (CPU/Memory)\n• Gateway & Actuator Metrics]
    end

    subgraph TracingFlow ["Distributed Tracing Pipeline"]
        ZIPKIN[Zipkin Server\nSpan Collection Endpoint]
    end

    Services -->|HTTP /actuator/prometheus| PROM
    PROM --> GRAF
    Services -->|Micrometer Tracer via HTTP POST| ZIPKIN
```

---

## Services

### Service Inventory

| Service | Responsibility | Port (Local / K8s) | Database | Primary Communication |
| :--- | :--- | :--- | :--- | :--- |
| `api-gateway-service` | Reverse proxy, rate limiting, JWT & API Key validation, correlation ID injection | `8080` / `8080` (NodePort `30080`) | None (Redis cache) | OpenFeign to `merchant-service` |
| `merchant-service` | Merchant onboarding, user login, API key lifecycle, webhook configs, customer directory | `8081` / `9010` | `merchant_db` | OpenFeign (Server), Spring Security |
| `payment-service` | Order records, payments, state machine, payment adapters, refunds, transactional outbox | `8082` / `9020` | `payment_db` | OpenFeign to `merchant`, `vault`; Kafka Outbox Producer |
| `vault-service` | Card data vault, AES-256 card encryption, card tokenization, direct charge simulator | `8083` / `9040` | `vault_db` | OpenFeign (Server from `payment-service`) |
| `operations-service` | Asynchronous settlement calculation, bank transfer simulation, webhook dispatching, retry queue & DLQ | `8084` / `9030` | `operations_db` | Kafka Consumer, OpenFeign to `merchant`, `payment` |
| `config-service` | Centralized external configuration repository integration via Spring Cloud Config | `8888` / `8888` | None | Git-backed configuration server |
| `discovery-service` | Service registration and heartbeat monitoring via Netflix Eureka | `8761` / `8761` | None | Eureka Client/Server protocol |
| `common-lib` | Shared domain entity bases, money type, enums, exception hierarchy, Redis rate limiters, security filters | N/A (Shared JAR) | N/A | Maven dependency across all services |

---

### API Gateway (`api-gateway-service`)
- **Entry Point & Security**: Built on Spring Cloud Gateway Server WebMVC. Serves as the sole entry point for all traffic. All inbound HTTP requests are filtered through `GatewayAuthFilter`.
- **Authentication Handlers**:
  - `PublicRouteMatcher`: Routes like `/api/v1/auth/**`, `/actuator/**`, and swagger definitions bypass authentication.
  - `JwtAuthHandler`: Parses `Bearer <JWT>`, verifies HMAC signatures using `jjwt`, and propagates authenticated headers `X-Merchant-Id` and `X-User-Role`.
  - `ApiKeyAuthHandler`: Decodes `Basic <base64(keyId:secret)>`, resolves keys from Redis or `ApiKeyLookupClient`, checks against BCrypt hashes, and enforces rotation grace periods.
- **Header Augmentation**: Uses `HeaderAugmentingRequestWrapper` to inject validated identity metadata (`X-Merchant-Id`, `X-Environment`, `X-Key-Id`) downstream.
- **Rate Limiting**: Enforces a per-key limit (default: 60 requests/min) before downstream dispatch.

### Merchant Service (`merchant-service`)
- **Merchant Onboarding**: Manages merchant profiles (`Merchant`), business metadata, and account statuses (`ACTIVE`, `SUSPENDED`).
- **User Authentication**: Authenticates administrative users (`AppUser`) and issues JWT tokens signed with HMAC-SHA256.
- **API Key Management**: Generates dual-environment (`TEST`, `LIVE`) API keys. Key secrets are hashed using BCrypt (`keySecretHash`). Supports automated 24-hour grace periods on key rotation (`previousKeySecretKeyHash`).
- **Customer Directory**: Manages customer profiles (`Customer`) identified by merchant scope, email, and phone.
- **Webhook Registry**: Stores target webhook URLs, subscribed event types, and encrypted signing secrets (`MerchantWebhookConfig`).

### Payment Service (`payment-service`)
- **Order Management**: Creates orders (`OrderRecord`) with receipt IDs, amount, currency, notes, and expiration timestamps.
- **Payment Lifecycle Execution**: Processes payment attempts (`Payment`) across Card, UPI, and NetBanking methods.
- **State Machine Enforcement**: All payment state transitions are guarded by `PaymentStateMachine`, with complete transition histories recorded in `PaymentTransitionLog`.
- **Payment Method Routing**: Routes payments via `PaymentGatewayRouter` to modular adapters:
  - `CardPaymentAdapter`: Invokes `vault-service` to tokenize and charge stored cards.
  - `UpiPaymentAdapter`: Simulates UPI VPA validation and callback resolution.
  - `NetBankingAdapter`: Simulates retail bank portal redirect workflows.
- **Bank Callback Simulator**: `BankCallbackSimulator` coordinates synthetic authorization approvals or rejections.
- **Transactional Outbox**: Emits `PAYMENT_STATUS_CHANGED`, `ORDER_CREATED`, and refund events to `outbox_event` within the primary ACID transaction.
- **Decoupled Persistence**: Uses `OrderPersistenceService` to isolate database transactions from external OpenFeign calls.

### Vault Service (`vault-service`)
- **Data Isolation**: Isolates primary account numbers (PAN), CVVs, and expiry dates from the general payment processing domain.
- **Cryptographic Storage**: Uses AES-256 field-level encryption (`VaultEncryptionConfig`) to store encrypted card records (`VaultCard`).
- **Tokenization**: Generates non-sensitive `CardToken` UUIDs with `last4` and brand metadata.
- **Direct Charge Processing**: Provides internal RPC endpoint (`InternalVaultController`) allowing `payment-service` to execute card charges without exposing raw card details outside the vault boundary.

### Operations Service (`operations-service`)
- **Kafka Consumption**: Listens on `payments.events`, `orders.events`, `refunds.events`, and `settlements.events`.
- **Webhook Dispatching**: `WebhookKafkaConsumer` matches incoming events against merchant webhook configs, signs the payload using HMAC-SHA256 (`SignerUtil`), and persists a `WebhookEvent` in status `PENDING`.
- **Retry Queue & Delivery**: `WebhookDeliverySchedular` and `WebhookDeliveryExecutor` dispatch HTTP POST calls with exponential backoff:
  - Intervals: 1m, 5m, 30m, 2h, 8h, 24h (Max 7 attempts).
- **Dead-Letter Handling**: Events exhausting all retry attempts are transitioned to `DEAD` and archived in `dlq_event` via `WebhookDlqRecorder`.
- **Settlement Engine**: `SettlementEngine` aggregates un-settled captured payments for active merchants, creates `Settlement` records, and executes bank payouts via `BankTransferProcessor` and `BankSettlementCallbackSimulator`.

### Config Service (`config-service`)
- **Centralized Properties**: Spring Cloud Config Server backed by external Git repository (`https://github.com/nexus-og-wb/distributed-razorpay-config`).
- **Profile Support**: Supplies environment-specific configurations (`dev`, `k8s`) for database credentials, Kafka endpoints, and Zipkin locations.

### Discovery Service (`discovery-service`)
- **Service Registry**: Netflix Eureka server enabling dynamic service registration and client-side load-balanced routing for local development.

### Common Library (`common-lib`)
- **Domain Fundamentals**: Exports `BaseEntity` (JPA auditing for createdAt/updatedAt), `Money` value object (immutable amount units and currency), and domain enums.
- **Rate Limiting Engine**: Provides `RateLimiter` interface with four implementations: `FixedWindowRateLimiter`, `SlidingWindowRateLimiter`, `SlidingWindowLuaLimiter`, and `TokenBucketRateLimiter`.
- **Idempotency Store**: Supplies `RedisIdempotencyStore` and `IdempotencyFilter` for header-based request caching.
- **Security & Caching**: Shared `ApiKeyCache` contracts, Redis cache implementations, AES encryption utilities, and `MerchantContextFilter`.
- **Exception Hierarchy**: Unified exception definitions (`BusinessRuleViolationException`, `RateLimitException`, `IdempotencyConflictException`, `InvalidStateTransitionException`) and `GlobalExceptionHandler`.

---

## Payment Lifecycle

```mermaid
sequenceDiagram
    autonumber
    actor Customer
    actor Merchant
    participant GW as api-gateway-service
    participant PS as payment-service
    participant MS as merchant-service
    participant VS as vault-service
    participant SIM as Bank Simulator
    participant DB as payment_db
    participant KF as Kafka (payments.events)
    participant OS as operations-service
    participant WH as Merchant Webhook URL

    Note over Merchant,PS: 1. Order Creation
    Merchant->>GW: POST /api/v1/orders (Basic API Key)
    GW->>PS: Forward with X-Merchant-Id
    PS->>MS: Find/Create Customer (Feign)
    MS-->>PS: Customer ID
    PS->>DB: INSERT order_record (status: CREATED) & outbox_event
    DB-->>PS: Order Persisted
    PS-->>Merchant: 201 Created (Order Response)

    Note over Customer,SIM: 2. Payment Initiation
    Customer->>GW: POST /api/v1/payments/initiate (orderId, method, details)
    GW->>PS: Forward with X-Idempotency-Key
    PS->>DB: Check Idempotency Key (idx_payment_merchant_idempotency)
    PS->>DB: INSERT payment (status: CREATED -> AUTHORIZING)
    
    alt Card Payment
        PS->>VS: POST /api/v1/internal/vault/charge (cardToken)
        VS-->>PS: Charge Authorized (bankReference)
    else UPI / NetBanking
        PS->>SIM: Route to Upi/NetBanking Adapter
        SIM-->>PS: Async Callback / Immediate Auth
    end

    Note over PS,DB: 3. Authorization & Auto-Capture
    PS->>DB: UPDATE payment (status: AUTHORIZED)
    PS->>DB: UPDATE payment (status: CAPTURING -> CAPTURED)
    PS->>DB: UPDATE order_record (status: PAID)
    PS->>DB: INSERT outbox_event (PAYMENT_STATUS_CHANGED)
    PS-->>Customer: 200 OK (Payment CAPTURED)

    Note over PS,WH: 4. Asynchronous Outbox & Webhook Dispatch
    loop Outbox Poller (Every 500ms)
        PS->>DB: SELECT * FROM outbox_event WHERE status = 'PENDING'
        PS->>KF: Publish message (Key: merchantId)
        PS->>DB: UPDATE outbox_event SET status = 'PUBLISHED'
    end

    KF->>OS: Consume PAYMENT_STATUS_CHANGED
    OS->>MS: Get Active Webhook Configs (Feign)
    MS-->>OS: Target URL & Webhook Secret
    OS->>OS: Compute HMAC-SHA256 Signature
    OS->>WH: HTTP POST Payload (X-Razorpay-Signature)
    WH-->>OS: 200 OK
    OS->>OS: Mark WebhookEvent DELIVERED
```

---

## Authentication & Authorization

Authentication is enforced at the API Gateway before requests reach downstream services:

```mermaid
flowchart TD
    REQ[Inbound HTTP Request] -->> GWF[GatewayAuthFilter]
    GWF -->> IS_PUB{Public Route?}
    
    IS_PUB -- Yes -->> DISPATCH[Forward Downstream]
    
    IS_PUB -- No -->> HDR_TYPE{Authorization Header Type}
    
    HDR_TYPE -- "Basic <Base64(Key:Secret)>" -->> AK[ApiKeyAuthHandler]
    HDR_TYPE -- "Bearer <JWT>" -->> JWT[JwtAuthHandler]
    HDR_TYPE -- Missing / Other -->> REJ401[Reject: 401 UNAUTHORIZED]

    JWT -->> VERIFY_JWT[Verify HMAC Signature via jjwt]
    VERIFY_JWT -->> INJECT_JWT[Inject: X-Merchant-Id, X-User-Role]
    INJECT_JWT -->> DISPATCH

    AK -->> CHECK_CACHE{Check Redis Verification Cache\napikey:secret-verified:keyId:...}
    
    CHECK_CACHE -- Cache Hit -->> INJECT_AK[Inject: X-Merchant-Id, X-Environment, X-Key-Id]
    
    CHECK_CACHE -- Cache Miss -->> LOAD_KEY[Load ApiKeyCacheEntry from Redis or Feign]
    LOAD_KEY -->> BCRYPT_CHECK{BCrypt.matches(secret, hash)\nor Grace Period Match?}
    
    BCRYPT_CHECK -- No -->> REJ_KEY[Reject: 401 UNAUTHORIZED]
    BCRYPT_CHECK -- Yes -->> PUT_CACHE[Cache Success in Redis\nTTL: 30 Seconds]
    PUT_CACHE -->> RL_CHECK{Check Rate Limit\napikey:keyId}
    
    RL_CHECK -- Exceeded -->> REJ429[Reject: 429 TOO_MANY_REQUESTS]
    RL_CHECK -- Allowed -->> INJECT_AK
    INJECT_AK -->> DISPATCH
```

### The API Key Verification Cache Optimization
- **Problem**: API key secrets are stored as BCrypt hashes. BCrypt is designed to be computationally expensive (work factor 10 involves $2^{10}$ iterations). Under a load test with 100–300 requests/second, the gateway CPU saturated verifying the exact same API key secret repeatedly.
- **Solution**: Once BCrypt successfully verifies a key secret, the result is cached in Redis under the key:
  ```
  apikey:secret-verified:{keyId}:{keySecretHash}:{SHA256(rawSecret)}
  ```
  with an expiration TTL of **30 seconds**.
- **Impact**: Subsequent requests hitting the gateway bypass the BCrypt hashing loop entirely via a sub-millisecond Redis `hasKey` check, dramatically relieving gateway CPU pressure while preserving credential rotation boundaries.

---

## Rate Limiting

The platform provides distributed rate limiting backed by Redis, located in `common-lib/ratelimit`.

### Implementations Available in Codebase

| Implementation | Mechanism | Characteristics |
| :--- | :--- | :--- |
| `FixedWindowRateLimiter` | Redis `INCR` + `EXPIRE` | Increments a counter keyed to the current time window block. Subject to boundary bursts. |
| `SlidingWindowRateLimiter` | Redis Sorted Sets (`ZSET`) | Records individual request timestamps. Prunes entries older than `now - windowSeconds`. Completely prevents boundary bursting. |
| `SlidingWindowLuaLimiter` | Atomic Redis Lua script | Executes sliding window calculation in a single round-trip to Redis. |
| `TokenBucketRateLimiter` | Redis Lua Token Bucket | Simulates continuous token refilling at a specified replenishment rate. Supports burst capacities. |

### Active Production Path Configuration
In the current request path (`ApiKeyAuthHandler`), the gateway enforces a rate limit per API key (default: **60 requests per minute**):
- When rate limit is exceeded, the gateway immediately returns `HTTP 429 Too Many Requests`.
- The response carries standard headers:
  - `X-RateLimit-Limit`: Maximum requests permitted per window.
  - `X-RateLimit-Remaining`: Count of requests remaining.
  - `Retry-After`: Seconds to wait before retrying.

---

## Idempotency

Mutating financial operations (`/orders`, `/payments/initiate`, `/payments/{id}/capture`, `/refunds`) must execute exactly once, even if networks drop or clients retry.

```mermaid
flowchart TD
    REQ[Mutating Request with X-Idempotency-Key] -->> CHK_REDIS{RedisIdempotencyStore\nKey: idempotency:merchantId:key}
    
    CHK_REDIS -- "Status: COMPLETED" -->> RETURN_CACHED[Return Cached Response Immediately\n(No DB or Network execution)]
    CHK_REDIS -- "Status: PROCESSING" -->> REJ_CONFLICT[Throw IdempotencyConflictException\nHTTP 409 Conflict]
    
    CHK_REDIS -- "Not Found" -->> ACQUIRE[Atomic SETNX: status = PROCESSING\nTTL: 86400s (24h)]
    ACQUIRE -->> EXEC[Execute Business Transaction]
    
    EXEC -->> DB_SAVE{Save to Database}
    DB_SAVE -- Unique Constraint Violated -->> HANDLE_EX[Catch DataIntegrityViolationException\nReplay Existing Entity]
    DB_SAVE -- Success -->> UPDATE_REDIS[Update Redis:\nstatus = COMPLETED, response = payload]
    UPDATE_REDIS -->> RESP[Return Fresh Response to Client]
```

### Multi-Layer Idempotency Guarantees
1. **Redis Idempotency Store (`RedisIdempotencyStore`)**: High-speed edge interceptor caching responses for 24 hours. Prevents unnecessary database roundtrips on rapid client replays.
2. **Database Unique Constraints**:
   - `payment`: Unique index `idx_payment_merchant_idempotency` on `(merchant_id, idempotency_key)`.
   - `order_record`: Unique index `idx_order_merchant_receipt` on `(merchant_id, receipt)`.
   If a race condition bypasses Redis (e.g., node failover), the PostgreSQL unique constraint rejects duplicate insertion at the database level.
3. **Idempotency Replay in Service**: `PaymentServiceImpl.initiate()` checks `paymentAuthorizationRecorder.findExistingAttempt(merchantId, idempotencyKey)` and safely replays previously recorded payment data.

---

## Payment State Machine

All transitions are governed deterministically by `com.prashant.razorpay.payment_service.statemachine.PaymentStateMachine`. Arbitrary database updates to payment status are disallowed.

```mermaid
stateDiagram-v2
    [*] --> CREATED

    CREATED --> AUTHORIZING: AUTHORIZE_ATTEMPT
    CREATED --> CANCELLED: CANCEL

    AUTHORIZING --> AUTHORIZED: AUTHORIZE_SUCCESS
    AUTHORIZING --> FAILED: AUTHORIZE_FAIL
    AUTHORIZING --> CANCELLED: CANCEL

    AUTHORIZED --> CAPTURING: CAPTURE_REQUEST
    AUTHORIZED --> AUTH_EXPIRED: CAPTURE_TIMEOUT

    CAPTURING --> CAPTURED: CAPTURE_SUCCESS
    CAPTURING --> AUTHORIZED: CAPTURE_FAIL

    CAPTURED --> PARTIALLY_REFUNDED: REFUND_INIT
    CAPTURED --> REFUNDED: REFUND_COMPLETE
    CAPTURED --> SETTLED: SETTLE

    PARTIALLY_REFUNDED --> REFUNDED: REFUND_COMPLETE

    SETTLED --> PARTIALLY_REFUNDED: REFUND_INIT

    FAILED --> [*]
    CANCELLED --> [*]
    REFUNDED --> [*]
    AUTH_EXPIRED --> [*]
```

### Transition Matrix

| Initial State | Event Trigger | Next State | Responsible Component |
| :--- | :--- | :--- | :--- |
| `CREATED` | `AUTHORIZE_ATTEMPT` | `AUTHORIZING` | `PaymentServiceImpl.initiate()` |
| `CREATED` | `CANCEL` | `CANCELLED` | Client abort before authorization |
| `AUTHORIZING` | `AUTHORIZE_SUCCESS`| `AUTHORIZED` | `PaymentServiceImpl.resolveAuthorization()` |
| `AUTHORIZING` | `AUTHORIZE_FAIL` | `FAILED` | Bank rejection simulator |
| `AUTHORIZING` | `CANCEL` | `CANCELLED` | Client abort during authorization |
| `AUTHORIZED` | `CAPTURE_REQUEST` | `CAPTURING` | Automatic capture or manual capture trigger |
| `AUTHORIZED` | `CAPTURE_TIMEOUT` | `AUTH_EXPIRED` | Uncaptured payment authorization expiry poller |
| `CAPTURING` | `CAPTURE_SUCCESS` | `CAPTURED` | Gateway confirmation callback |
| `CAPTURING` | `CAPTURE_FAIL` | `AUTHORIZED` | Capture network failure (allows retry) |
| `CAPTURED` | `SETTLE` | `SETTLED` | `SettlementEngine` batch execution |
| `CAPTURED` | `REFUND_INIT` | `PARTIALLY_REFUNDED`| Partial refund initiated |
| `CAPTURED` | `REFUND_COMPLETE`| `REFUNDED` | Full refund executed |
| `PARTIALLY_REFUNDED`| `REFUND_COMPLETE`| `REFUNDED` | Remaining balance refunded |
| `SETTLED` | `REFUND_INIT` | `PARTIALLY_REFUNDED`| Refund processed post-settlement |

Every state transition writes an immutable audit record to `payment_transition_log` with the previous status, next status, trigger event, and actor (`SYSTEM`, `MERCHANT`, `CUSTOMER`).

---

## Kafka & Event-Driven Architecture

### The Dual-Write Problem & Transactional Outbox
In distributed systems, updating a database and publishing to Kafka cannot be coordinated in a single ACID transaction without expensive two-phase commit protocols. If the database commit succeeds but the Kafka broker is down, the event is permanently lost.

The platform resolves this using the **Transactional Outbox Pattern**:
1. When a payment state transitions, the database updates the `payment` table and writes an event payload to `outbox_event` in the **same local database transaction**.
2. If the transaction rolls back, neither the payment change nor the event persists.
3. A background daemon (`OutboxPoller`) runs periodically (every 500ms), scanning for events with `status = 'PENDING'` using PostgreSQL row-level locks:
   ```sql
   SELECT * FROM outbox_event WHERE status = 'PENDING' ORDER BY created_at ASC LIMIT 50 FOR UPDATE SKIP LOCKED
   ```
4. The poller publishes each event to Kafka using `merchantId` as the partition key.
5. Upon receiving an acknowledgment from Kafka, the poller updates `outbox_event.status = 'PUBLISHED'`.
6. If Kafka publication fails, the record remains `PENDING` and is retried on the next poll cycle.

### Kafka Topic Directory

| Topic Configuration Property | Default Topic Name | Producer Service | Consumers / Consumer Groups | Partition Key |
| :--- | :--- | :--- | :--- | :--- |
| `app.kafka.topics.payments` | `payments.events` | `payment-service` | `operations-service` (`operations-webhook-group`, `operations-settlement-group`) | `merchantId` |
| `app.kafka.topics.orders` | `orders.events` | `payment-service` | `operations-service` (`operations-webhook-group`) | `merchantId` |
| `app.kafka.topics.refunds` | `refunds.events` | `payment-service` | `operations-service` (`operations-webhook-group`) | `merchantId` |
| `app.kafka.topics.settlements` | `settlements.events` | `operations-service` | `operations-service` (`operations-webhook-group`) | `merchantId` |

---

## Transaction Management & Consistency

### Isolating External Calls from Relational Transactions
Holding a database transaction open while performing external network calls (such as OpenFeign RPCs or bank API invocations) is a classic distributed systems anti-pattern. If the remote service stalls or experiences latency spikes, the database connection pool is quickly exhausted, cascading failure across the entire service.

In `payment-service`, this boundary is explicitly separated:
1. `OrderServiceImpl.create()` is marked with `@Transactional(propagation = Propagation.NOT_SUPPORTED)`.
2. The external OpenFeign call to `CustomerServiceClient` executes **outside** of any database transaction.
3. Once the customer lookup finishes, execution delegates to `OrderPersistenceService.persist()`, which opens a tight, short-lived `@Transactional` boundary exclusively for saving the `OrderRecord` and writing the `OutboxEvent`.

---

## Webhooks

```mermaid
flowchart TD
    EVT[Event Consumed from Kafka] --> LOOKUP[Fetch Active Webhook Configurations for Merchant]
    LOOKUP --> SIGN[Compute HMAC-SHA256 Signature\nSecret: webhookSecret]
    SIGN --> SAVE[Save WebhookEvent to operations_db\nStatus: PENDING]
    SAVE --> Q[Enqueue to WebhookRetryQueue\nPriorityQueue sorted by nextRetryAt]

    Q --> SCHED[WebhookDeliverySchedular (Fixed Delay: 5s)]
    SCHED --> EXEC[WebhookDeliveryExecutor]
    EXEC --> POST[HTTP POST to Merchant Target URL\nHeader: X-Razorpay-Signature]

    POST --> EVAL{Response Status}
    EVAL -- "2xx OK" --> OK[Update Status: DELIVERED\nSet deliveredAt]
    EVAL -- "Non-2xx / Timeout" --> RETRY_CHECK{Attempts < 7?}

    RETRY_CHECK -- Yes --> BACKOFF[Calculate Exponential Backoff\n1m, 5m, 30m, 2h, 8h, 24h]
    BACKOFF --> RE_ENQ[Update nextRetryAt\nRe-enqueue to WebhookRetryQueue]
    
    RETRY_CHECK -- No --> DLQ[Update Status: DEAD\nWrite to dlq_event table via WebhookDlqRecorder]
```

### Webhook Delivery Specification
- **Security**: Dispatched payloads contain an `X-Razorpay-Signature` header computed via `HMAC-SHA256(payload, webhookSecret)`.
- **Retry Schedule**: Exponential backoff across 7 attempts: 1 minute, 5 minutes, 30 minutes, 2 hours, 8 hours, 24 hours.
- **Dead-Letter Handling**: If all 7 attempts fail, the webhook is transitioned to `DEAD`, and failure details (`lastResponseBody`, `lastResponseCode`) are written to `dlq_event`.

---

## Settlement

```mermaid
flowchart LR
    SCHED[Scheduled Trigger / Operations Cron] --> ENG[SettlementEngine]
    ENG --> QUERY[Query Captured Payments for Merchant]
    QUERY --> CALC[Compute Gross Volume, Fees & Net Settlement Amount]
    CALC --> DB_SET[Save Settlement & SettlementPayment records]
    DB_SET --> PROC[BankTransferProcessor]
    PROC --> SIM[BankSettlementCallbackSimulator]
    SIM --> COMPLETED[Mark Settlement SETTLED\nRecord UTR Reference]
    COMPLETED --> OUTBOX[Emit SETTLEMENT_COMPLETED to Outbox]
```

### Multi-Replica Scheduling Considerations
When multiple replicas of `operations-service` run in Kubernetes, scheduled jobs like the settlement batch runner or outbox poller must not run concurrently on identical datasets. While the outbox poller uses PostgreSQL row-level locks (`FOR UPDATE SKIP LOCKED`), dependencies for distributed scheduling (`shedlock-spring`, `shedlock-provider-redis-spring`) are declared in `pom.xml` to allow distributed locking across replicas.

---

## Vault & Sensitive Data

```mermaid
flowchart LR
    subgraph PaymentZone ["Payment Processing Zone"]
        PS[payment-service]
    end

    subgraph VaultZone ["Card Data Vault Zone"]
        VC[InternalVaultController]
        VS[VaultServiceImpl]
        ENC[AES-256 GCM Encryption]
        VDB[(vault_db)]
    end

    PS -->|1. Tokenize / Charge Request| VC
    VC --> VS
    VS -->|2. Encrypt Card Number| ENC
    ENC -->|3. Persist Encrypted Payload| VDB
    VS -- 4. Return Non-sensitive CardToken UUID --> PS
```

### Vault Security Features
- **Field-Level AES-256 Encryption**: Primary Account Numbers (PAN) and cardholder names are encrypted before persistence in `vault_db` using a master secret key.
- **Tokenization**: Downstream services only store and transmit random UUID tokens (`CardToken`) linked to the encrypted record.
- **Isolation Scope**: `vault-service` runs on its own dedicated PostgreSQL database (`vault_db`) with separate database credentials and isolated internal routes.
- **Clarification**: This architecture models tokenization design patterns for portfolio and academic purposes; it is not certified under formal PCI-DSS Level 1 compliance programs.

---

## Database Architecture

The system enforces a strict **Database-per-Service** pattern. Microservices maintain exclusive ownership over their relational schemas.

```mermaid
erDiagram
    %% merchant_db
    MERCHANT ||--o{ APP_USER : "has"
    MERCHANT ||--o{ API_KEY : "issues"
    MERCHANT ||--o{ MERCHANT_WEBHOOK_CONFIG : "registers"
    MERCHANT ||--o{ CUSTOMER : "manages"

    %% payment_db
    ORDER_RECORD ||--o{ PAYMENT : "associates"
    PAYMENT ||--o{ PAYMENT_TRANSITION_LOG : "audits"
    PAYMENT ||--o{ REFUND : "processes"
    PAYMENT_DB ||--o{ OUTBOX_EVENT : "stores"

    %% vault_db
    VAULT_CARD ||--o{ CARD_TOKEN : "tokenizes"

    %% operations_db
    SETTLEMENT ||--o{ SETTLEMENT_PAYMENT : "contains"
    OPERATIONS_DB ||--o{ WEBHOOK_EVENT : "delivers"
    OPERATIONS_DB ||--o{ DLQ_EVENT : "records"
```

### Relational Schema Summary

| Database | Service Owner | Primary Tables | Key Constraints & Indexes |
| :--- | :--- | :--- | :--- |
| `merchant_db` | `merchant-service` | `merchant`, `app_user`, `api_key`, `merchant_webhook_config`, `customer` | `api_key.key_id` (Unique), `app_user.email` (Unique) |
| `payment_db` | `payment-service` | `order_record`, `payment`, `payment_transition_log`, `refund`, `outbox_event` | `(merchant_id, idempotency_key)` (Unique), `(merchant_id, receipt)` (Unique), `payment.order_id` (Index) |
| `vault_db` | `vault-service` | `vault_card`, `card_token` | `card_token.token` (Unique) |
| `operations_db`| `operations-service` | `settlement`, `settlement_payment`, `webhook_event`, `dlq_event`, `outbox_event`| Composite PK `(settlement_id, payment_id)` |

---

## Redis Architecture

Redis is deployed as an in-memory coordination and caching layer across multiple subsystems:

```mermaid
graph TD
    REDIS[("Redis (Port 6379)")]
    
    subgraph Uses ["Active Redis Subsystems"]
        U1["API Key Verification Cache\nKey: apikey:secret-verified:*\nTTL: 30 Seconds\nBypasses CPU-bound BCrypt"]
        U2["Distributed Rate Limiting\nKey: apikey:*\nSliding / Fixed Window counters\nEnforces 60 req/min limits"]
        U3["Idempotency Store\nKey: idempotency:*\nTTL: 24 Hours\nPrevents duplicate payment execution"]
        U4["API Key Entity Cache\nKey: apikey:cache:*\nTTL: 5 Minutes\nReduces Feign lookup traffic"]
    end

    REDIS --- U1
    REDIS --- U2
    REDIS --- U3
    REDIS --- U4
```

---

## Resilience

### Fault-Tolerance Patterns in the Codebase
- **Circuit Breakers & Retries**: Declared using Resilience4j annotations on inter-service Feign clients (e.g., `@CircuitBreaker(name = "merchant-service")` and `@Retry(name = "merchant-service")` on `CustomerServiceClient`).
- **HTTP/2 Connection Pooling**: Upgraded Feign client to `feign-hc5` (Apache HttpClient 5) to support persistent connection pooling and eliminate connection exhaustion during traffic bursts.
- **Fail-Safe Fallbacks**: During gateway authentication, if the Redis cache fails, the gateway gracefully falls back to the direct Feign lookup to ensure operational continuity.
- **Database Row-Level Locking**: High-concurrency operations use explicit PostgreSQL locking:
  - `PaymentRepository.findByIdForUpdate()` uses `PESSIMISTIC_WRITE` to prevent lost updates during simultaneous authorization or capture calls.
  - `OutboxPoller` uses `SKIP LOCKED` to prevent lock contention across concurrent poller threads.

---

## Distributed Scheduling

Scheduled tasks across the platform include:
1. **Outbox Poller (`OutboxPoller`)**: Fixed 500ms delay polling `outbox_event` table in `payment-service` and `operations-service`.
2. **Webhook Dispatcher (`WebhookDeliverySchedular`)**: Fixed 5000ms delay scanning priority queue in `operations-service`.
3. **Capture Timeout & Expiry Watcher**: Periodic checks releasing funds for payments that remain uncaptured beyond the authorized window.

---

## Kubernetes Deployment

Deployments are orchestrated on **Kubernetes (via Kind for local testing)** using declarative manifests organized with Kustomize.

```
k8s/
??? kind-config.yaml              # Kind cluster spec (NodePort mapping 30080 -> 80)
??? kustomization.yaml            # Master Kustomize overlay
??? k8s-secrets.env               # Base environment secret configuration
??? infra/
?   ??? namespace.yaml            # namespace: razorpay-core
?   ??? configmap.yaml            # Shared configuration (app-config)
??? services/
?   ??? api-gateway.yaml          # Deployment (4 CPU / 6Gi) + NodePort Service
?   ??? config-service.yaml       # Deployment (500m / 768Mi) + ClusterIP Service
?   ??? merchant-service.yaml     # Deployment (4 CPU / 6Gi) + ClusterIP Service
?   ??? payment-service.yaml      # Deployment (4 CPU / 8Gi) + ClusterIP Service
?   ??? operations-service.yaml   # Deployment (1 CPU / 2Gi) + ClusterIP Service
?   ??? vault-service.yaml        # Deployment (1 CPU / 2Gi) + ClusterIP Service
??? stateful/
    ??? postgres.yaml             # PostgreSQL 16 Deployment + Persistent Volume
    ??? redis.yaml                # Redis Deployment + ClusterIP Service
    ??? kafka.yaml                # Apache Kafka KRaft Single-Node Deployment
    ??? kafka-ui.yaml             # Kafka UI Management Dashboard (Port 8090)
    ??? prometheus.yaml           # Prometheus Scraper Deployment (Port 9090)
    ??? grafana.yaml              # Grafana Dashboard Engine (Port 3000)
    ??? zipkin.yaml               # Zipkin Distributed Tracing (Port 9411)
```

---

## Observability

### Metrics & Dashboards
Every microservice exposes Micrometer metrics at `/actuator/prometheus`.
- **Prometheus Scrapes**: Configured via ConfigMap scraping all core services every 10 seconds.
- **Grafana Provisioning**: Automatically provisions:
  - Datasource pointing to Prometheus (`http://prometheus:9090`).
  - Pre-configured dashboard: **Service CPU & Memory** (`service-resources.json`), charting real-time JVM memory allocations and process CPU utilization percentages.

### Distributed Tracing
- All services integrate `spring-boot-starter-zipkin` and `zipkin-sender-urlconnection`.
- Traces propagate `traceId` and `spanId` across OpenFeign calls, allowing end-to-end request tracing in the Zipkin UI (`http://localhost:9411`).

---

## Performance & Load Testing

Load testing was conducted locally against the Kind Kubernetes cluster to empirically evaluate system throughput, identify resource bottlenecks, and tune system concurrency.

### Test Environment
- **Host Infrastructure**: Local development machine running Docker Desktop and Kind Kubernetes cluster.
- **Cluster Provisioning**: Single-node Kind cluster running all stateful backends (PostgreSQL, Redis, Kafka) and microservice pods.
- **Workload Profile**: Synthetic HTTP POST requests generating authenticated orders and payments via API Gateway under varying concurrency (100–300 requests/second).

---

## Performance Optimization Case Study

### BCrypt Authentication Bottleneck & 86% Latency Reduction

During initial load testing, the API Gateway experienced severe latency degradation:

1. **Diagnosis**: Profiled gateway metrics revealed **~100% CPU utilization** on the `api-gateway` pod while downstream microservices remained largely idle.
2. **Root Cause**: Every incoming request authenticated using HTTP Basic API keys required verifying the raw key secret against a stored BCrypt hash via `BCryptPasswordEncoder.matches()`. Because BCrypt is deliberately CPU-intensive, processing continuous concurrent requests exhausted the gateway's compute capacity, creating an artificial request queue.
3. **Optimization Implemented**: Introduced an in-memory Redis verification cache (`ApiKeyAuthHandler`). Upon successful BCrypt verification, a verification fingerprint is stored in Redis:
   ```java
   String cacheKey = "apikey:secret-verified:" + keyId + ":" + keySecretHash + ":" + sha256(rawSecret);
   stringRedisTemplate.opsForValue().set(cacheKey, "true", Duration.ofSeconds(30));
   ```
4. **Result**: On repeated requests, the gateway checks Redis in sub-milliseconds rather than recalculating the BCrypt hash.
5. **Measured Outcome**:

| Metric | Before Optimization | After Optimization | Percentage Improvement |
| :--- | :---:| :---:| :---:|
| **P99 Latency** | **~16.000 s** | **~2.221 s** | **~86.1% Reduction** |
| **Gateway CPU Saturation** | 100% (Bottleneck) | Dramatically Reduced | Stabilized |
| **BCrypt Hash Operations** | On every request | Once per 30-second window | >95% Elimination |

> *Note: These figures reflect performance measurements obtained within the local Kind test environment under the synthetic benchmark workload and serve as an architectural case study rather than a guaranteed production SLA.*

### External Network Call Extraction from Database Transactions
- **Problem**: `OrderServiceImpl.create()` previously held an active database transaction while executing an OpenFeign call to `CustomerServiceClient`. Under load, slow response times on customer lookups kept PostgreSQL connections occupied.
- **Fix**: The service method was decoupled into `OrderServiceImpl` (marked `Propagation.NOT_SUPPORTED`) and `OrderPersistenceService` (which manages the short-lived `@Transactional` persistence boundary).
- **Impact**: Database connection pool hold time was reduced from the entire duration of the remote RPC down to the few milliseconds required for local SQL inserts.

---

## Resource & Capacity Planning

The following compute allocations were configured in `k8s/services/` to accommodate local load testing workloads:

| Service Pod | CPU Requests | CPU Limits | Memory Requests | Memory Limits |
| :--- | :---:| :---:| :---:| :---:|
| `api-gateway` | 250m | 4.0 cores | 512 Mi | 6 Gi |
| `payment-service` | 250m | 4.0 cores | 512 Mi | 8 Gi |
| `merchant-service` | 250m | 4.0 cores | 512 Mi | 6 Gi |
| `operations-service` | 250m | 1.0 core | 512 Mi | 2 Gi |
| `vault-service` | 250m | 1.0 core | 512 Mi | 2 Gi |
| `config-service` | 250m | 500m | 512 Mi | 768 Mi |
| `postgres` (Stateful) | 250m | 1.0 core | 512 Mi | 1536 Mi |

Additionally, PostgreSQL connection limits were elevated (`max_connections=300`) to support concurrent pooling across multiple microservice HikariCP pools.

---

## Project Structure

```
distributed-payment-gateway-razorPay-clone/
??? api-gateway-service/          # Spring Cloud Gateway WebMVC edge proxy
?   ??? src/main/java/com/prashant/razorpay/api_gateway_service/
?       ??? client/               # OpenFeign client for ApiKey lookup
?       ??? security/             # GatewayAuthFilter, ApiKeyAuthHandler, JwtAuthHandler
??? common-lib/                   # Shared library across all microservices
?   ??? src/main/java/com/prashant/razorpay/common_lib/
?       ??? cache/                # ApiKeyCache interfaces and Redis cache implementation
?       ??? context/              # ThreadLocal MerchantContext & Filter
?       ??? entity/               # BaseEntity (auditing), Money value object
?       ??? enums/                # PaymentStatus, PaymentEvent, OrderStatus, etc.
?       ??? exceptions/           # Global exception hierarchy and handler
?       ??? idempotency/          # IdempotencyFilter & RedisIdempotencyStore
?       ??? ratelimit/            # Fixed, Sliding Window & Token Bucket limiters
??? config-service/               # Spring Cloud Config Server (Git-backed)
??? discovery-service/            # Netflix Eureka Service Registry
??? merchant-service/             # Merchant account, auth, API key, and customer domain
??? payment-service/              # Core payment processing, orders, state machine, outbox
??? vault-service/                # Card tokenization and AES-256 encrypted vault store
??? operations-service/           # Webhook delivery engine, retries, DLQ, settlements
??? k8s/                          # Kubernetes deployment manifests for Kind
?   ??? infra/                    # Shared Namespace and ConfigMap
?   ??? services/                 # Microservice deployment manifests
?   ??? stateful/                 # Postgres, Redis, Kafka, Monitoring manifests
??? observability/                # Prometheus, Grafana, and Zipkin compose configs
??? README.md                     # Authoritative system documentation
```

---

## Technology Stack

| Category | Technology | Purpose |
| :--- | :--- | :--- |
| **Language & Runtime** | Java 25 (OpenJDK) | Core backend programming language |
| **Framework** | Spring Boot 4.1.x | Microservice application framework |
| **Cloud Infrastructure**| Spring Cloud 2025.1.3 | Config Server, Eureka Discovery, OpenFeign |
| **Edge Gateway** | Spring Cloud Gateway Server WebMVC | Edge reverse proxy, auth filters, rate limiting |
| **Data Persistence** | PostgreSQL 16 | Relational persistence (Database-per-service) |
| **ORM & Auditing** | Spring Data JPA / Hibernate | Object-relational mapping, JSONB mapping, JPA auditing |
| **Distributed Cache** | Redis 7 (Alpine) | Idempotency store, rate limit windows, API key cache |
| **Event Streaming** | Apache Kafka 3.7 (KRaft) | Asynchronous event bus for outbox event delivery |
| **Security & Crypto** | Spring Security, jjwt, BCrypt, AES-256 | Authentication, JWT parsing, password & card crypto |
| **Resilience & Client** | Resilience4j, Feign-HC5 | Circuit breaking, retries, HTTP connection pooling |
| **Orchestration** | Kubernetes (Kind), Kustomize | Container orchestration and deployment manifests |
| **Container Build** | Google Jib Maven Plugin | Fast, daemonless container image generation |
| **Metrics** | Prometheus, Micrometer | Metric instrumentation and time-series scraping |
| **Dashboards** | Grafana 10.4 | Visualization of JVM memory, CPU, and request rates |
| **Distributed Tracing** | Zipkin, Micrometer Tracing | Distributed trace collection across Feign boundaries |

---

## Local Development

### Prerequisites
- **JDK 25** installed with `JAVA_HOME` configured
- **Apache Maven 3.9+**
- Local instances of **PostgreSQL 16**, **Redis 7**, and **Apache Kafka** running locally (or via Docker)

### Startup Sequence
Services must be initialized in order to satisfy configuration resolution and service discovery:

```bash
# 1. Start Config Server
cd config-service
./mvnw spring-boot:run

# 2. Start Eureka Discovery Server
cd ../discovery-service
./mvnw spring-boot:run

# 3. Start Core Domain Services (in separate terminals)
cd ../merchant-service && ./mvnw spring-boot:run
cd ../payment-service && ./mvnw spring-boot:run
cd ../vault-service && ./mvnw spring-boot:run
cd ../operations-service && ./mvnw spring-boot:run

# 4. Start API Gateway
cd ../api-gateway-service && ./mvnw spring-boot:run
```

---

## Kubernetes Setup (Kind)

To deploy the entire distributed system locally onto a Kind cluster:

```bash
# 1. Create the Kind cluster with NodePort mappings
kind create cluster --config k8s/kind-config.yaml

# 2. Build container images using Jib
cd api-gateway-service && ./mvnw compile jib:build && cd ..
cd merchant-service && ./mvnw compile jib:build && cd ..
cd payment-service && ./mvnw compile jib:build && cd ..
cd operations-service && ./mvnw compile jib:build && cd ..
cd vault-service && ./mvnw compile jib:build && cd ..
cd config-service && ./mvnw compile jib:build && cd ..

# 3. Create secrets from env template
kubectl create secret generic app-secrets \
  --from-env-file=k8s/k8s-secrets.env \
  -n razorpay-core

# 4. Apply Kustomize manifests
kubectl apply -k k8s/

# 5. Verify all pods are running
kubectl get pods -n razorpay-core

# 6. Access services
# API Gateway:  http://localhost:80 (via NodePort 30080)
# Prometheus:   http://localhost:9090
# Grafana:      http://localhost:3000 (admin / admin)
# Zipkin:       http://localhost:9411
# Kafka UI:     http://localhost:8090
```

---

## API Documentation

### Major API Endpoints

#### Merchant APIs (`merchant-service`)
- `POST /api/v1/auth/signup` — Register a new merchant and administrator account (Public)
- `POST /api/v1/auth/login` — Authenticate merchant and issue JWT bearer token (Public)
- `POST /api/v1/api-keys` — Generate a new API Key pair for TEST or LIVE environment (JWT)
- `POST /api/v1/api-keys/rotate` — Rotate API key with a 24-hour grace period for the previous key (JWT)
- `POST /api/v1/webhook-configs` — Register merchant webhook destination URL and subscribed events (JWT)

#### Payment APIs (`payment-service`)
- `POST /api/v1/orders` — Create a new order with receipt ID and amount (API Key or JWT)
- `GET /api/v1/orders/{orderId}` — Retrieve order details and status (API Key or JWT)
- `POST /api/v1/payments/initiate` — Initiate a payment attempt across Card, UPI, or NetBanking (Idempotent)
- `POST /api/v1/payments/{paymentId}/capture` — Manually capture an authorized payment (API Key or JWT)
- `POST /api/v1/payments/{paymentId}/refund` — Initiate a full or partial refund (API Key or JWT)
- `GET /api/v1/payments/{paymentId}` — Inspect full payment state and transition logs (API Key or JWT)

#### Vault APIs (`vault-service`)
- `POST /api/v1/vault/cards/tokenize` — Tokenize raw credit/debit card details into a `CardToken` UUID (Public / Scoped)
- `POST /api/v1/internal/vault/charge` — Internal endpoint for `payment-service` to process a card charge

#### Operations APIs (`operations-service`)
- `POST /api/v1/operations/settlements/execute` — Trigger batch settlement calculation for eligible merchants (Admin)
- `GET /api/v1/operations/webhooks/history` — Inspect webhook delivery attempts, HTTP status codes, and DLQ entries (JWT)

---

## Security Considerations

- **Credential Boundaries**: API key secrets and JWT signing secrets must never be committed to source repositories. They are supplied through Kubernetes Secrets (`app-secrets`).
- **Cryptographic Storage**: API key secrets are verified using BCrypt hashes; stored credit card information is encrypted at rest using AES-256.
- **Gateway Boundary**: Downstream domain microservices do not open public ingress ports; all external network traffic is funneled through `api-gateway-service`.
- **Educational Context**: Card handling in this project illustrates architectural separation patterns and does not constitute a certified PCI-DSS Level 1 compliant cardholder data environment.

---

## Known Limitations

- **Simulated Payment Networks**: Bank callbacks, UPI verification, and card network clearing are handled via simulator classes (`BankCallbackSimulator`, `BankSettlementCallbackSimulator`) rather than live banking APIs.
- **Local Kind Deployment**: Kubernetes configurations are optimized for local testing on a single Kind node rather than a managed, multi-region cloud deployment (e.g., AWS EKS or GCP GKE).
- **In-Memory Priority Queue for Webhook Retries**: `WebhookRetryQueue` schedules retries using an in-memory `PriorityQueue`. In a production multi-replica setup, this should be backed by a persistent distributed queue or Kafka delay topic.
- **Test Suite Scope**: Unit and context-loading tests (`*ApplicationTests.java`) verify basic application bootstrap; comprehensive automated end-to-end integration test suites across services remain a future extension.

---

## Engineering Tradeoffs

### 1. Microservices vs. Modular Monolith
- **Chosen**: Distributed microservices with Database-per-Service.
- **Benefits**: Independent scalability, isolated failure boundaries, and zero schema contention.
- **Costs**: Distributed tracing complexity, eventual consistency latency, and network overhead.

### 2. Transactional Outbox Pattern vs. Synchronous Event Publishing
- **Chosen**: Transactional Outbox with background polling.
- **Benefits**: The Transactional Outbox removes the classic application-level database/Kafka dual-write inconsistency by persisting the event alongside the business transaction and retrying publication. Payments commit reliably to PostgreSQL even if Kafka is temporarily degraded or offline.
- **Costs**: Minor delay (up to 500ms) between local database commit and Kafka event availability.

### 3. Redis BCrypt Verification Caching vs. Direct Hashing
- **Chosen**: 30-second Redis verification cache for successful API key validations.
- **Benefits**: ~86.1% reduction in P99 latency in local benchmarks (~16.000s to ~2.221s) by bypassing repeated BCrypt checks for cached keys.
- **Costs**: Key revocations take up to 30 seconds to propagate to the cache.

### 4. Database-per-Service vs. Shared Database
- **Chosen**: Dedicated database per service (`merchant_db`, `payment_db`, `vault_db`, `operations_db`).
- **Benefits**: Enforces strict domain boundaries; one service cannot execute foreign keys or cross-table joins against another's data.
- **Costs**: Cross-domain queries require asynchronous event projection or synchronous OpenFeign calls.

---

## What This Project Demonstrates

- **Distributed Systems Architecture**: Practical decomposition of complex domain logic into decoupled microservices.
- **Event-Driven Coordination**: Reliable asynchronous communication using Apache Kafka and the Transactional Outbox Pattern.
- **Financial Consistency & Reliability**: Explicit state machines, multi-layer idempotency (Redis + database uniqueness constraints), and pessimistic locking.
- **Performance Profiling & Bottleneck Resolution**: Identifying CPU bottlenecks under load and designing targeted caching solutions.
- **Production-Style Observability**: Complete telemetry integration combining Actuator, Prometheus, Grafana, and Zipkin distributed tracing.
- **Containerization & Kubernetes Orchestration**: Declarative cluster deployments with Kind, Jib container builds, ConfigMaps, and Secrets.

---

## Future Improvements

- **Distributed Delay Queues for Webhooks**: Replace in-memory priority queue retries with Kafka topic-based backoff queues.
- **Expanded Resilience4j Circuit Breaking**: Add circuit breakers and fallback isolation to all Feign client calls.
- **Automated Integration Test Harness**: Build an end-to-end integration test suite using Testcontainers for Kafka, PostgreSQL, and Redis.
- **Customer Checkout UI**: Implement a lightweight, responsive checkout interface consuming the payment gateway API contracts.

---

# Scaling the Architecture to 10,000 TPS

> [!IMPORTANT]
> **Capacity Planning vs. Current Benchmark Disclaimer**:  
> The current system has been empirically tested in a single-node Kind Kubernetes environment under synthetic loads (100–300 requests/second). The platform **does not claim** to operate at 10,000 TPS in its current local deployment.  
> Rather, the project includes a **10,000 TPS capacity-planning model** that estimates the horizontal scaling, database architecture, Kafka capacity, Redis clustering, connection pooling, and Kubernetes resources required to target that workload.  
>  
> *These figures are illustrative starting points for capacity planning, not experimentally validated production sizing. Final replica counts, database topology, Kafka partition counts, Redis topology, and infrastructure capacity must be validated through distributed load testing, workload characterization, and production telemetry.*

---

## 10,000 TPS Traffic Model

In payment gateway architectures, processing a completed transaction involves multiple distinct HTTP ingress operations. In this project's capacity-planning model, a standard customer payment transaction requires approximately:

1. **Order Creation (`POST /api/v1/orders`)**: Merchant backend or client initiates an order record.
2. **Payment Initiation (`POST /api/v1/payments/initiate`)**: Customer checkout submits payment details for authorization and automated capture.

This is not presented as a universal payment-industry rule, but represents the specific traffic model adopted for this project's capacity planning exercise. Under this model, targeting **10,000 completed payment transactions per second** corresponds to approximately:

$$\approx 20,000 \text{ HTTP requests/second}$$

entering the edge API Gateway.

```
10,000 payments/sec
        |
        +---- Create Order
        |
        +---- Init Payment
        |
        v
~20,000 HTTP requests/sec
```

---

## Horizontal Scaling Strategy

The core business services (`api-gateway-service`, `payment-service`, `merchant-service`, `vault-service`, `operations-service`) are architected to be **stateless**. No user session state, rate-limiting windows, idempotency locks, or transactional balances are pinned to JVM heap memory across requests. All state is maintained in external shared tiers (Redis, Kafka, and PostgreSQL).

```mermaid
flowchart TD
    CLIENT["Client Traffic (~20,000 HTTP req/sec)"] --> LB["Load Balancer"]
    LB --> GW["API Gateway Fleet"]
    
    subgraph GatewayFleet ["API Gateway Replicas"]
        G1["G1"]
        G2["G2"]
        G3["G3"]
        G4["G4"]
    end
    
    GW --> G1 & G2 & G3 & G4
    
    G1 & G2 & G3 & G4 --> PS["Payment Service Fleet"]
    
    subgraph PaymentFleet ["Payment Service Replicas"]
        P1["P1"]
        P2["P2"]
        P3["P3"]
        P4["P4"]
        P5["P5"]
    end
    
    PS --> P1 & P2 & P3 & P4 & P5
    P1 & P2 & P3 & P4 & P5 --> KF["Kafka Cluster"]
```

### Stateless Service Scaling Capabilities
- **API Gateway**: Horizontally scalable behind external load balancers to distribute ingress traffic and TLS termination.
- **Payment Service**: Replicas scale independently to process order persistence, state machine transitions, and adapter routing.
- **Merchant Service**: Replicas scale to handle administrative requests, customer lookups, and API key queries.
- **Vault Service**: Replicas scale to perform AES-256 encryption and tokenization without bottlenecking core payments.
- **Operations Service**: Replicas scale to consume Kafka topics and dispatch webhook notifications in parallel.
- **Shared Coordination**: Redis, Kafka, and PostgreSQL provide the required shared distributed state across replicas.

### Downstream Dependency Bottlenecks
Stateless service pods scale horizontally with ease, but **simply adding replicas does not automatically guarantee 10,000 TPS**. As application instances multiply, downstream shared state tiers become the primary limiting factors:
- **Relational database transaction throughput and disk I/O**
- **Database connection limits and lock contention**
- **Redis single-thread throughput and network bandwidth**
- **Kafka partition counts and broker I/O**
- **CPU saturation from cryptographic hashing (BCrypt) and encryption (AES-256)**

---

## PostgreSQL Scaling

At 10,000 TPS, the database becomes the most critical capacity constraint:
- 10,000 orders/sec + 10,000 payments/sec = **20,000 primary write transactions/sec**.
- Corresponding outbox writes = **20,000 outbox row inserts/sec**.
- Outbox polling updates = **20,000 status update writes/sec**.
- Audit log writes (`payment_transition_log`) = **30,000+ audit inserts/sec**.

This sheer transaction volume creates immense pressure across physical resources:

```
10,000 TPS
   ?
Large number of DB operations
   ?
Connection pressure
   ?
CPU / I/O pressure
   ?
Need for pooling + replicas + partitioning/sharding
```

### The Database Scaling Progression
Scaling PostgreSQL follows a structured architectural evolution:

```
Single PostgreSQL
       ?
Connection pooling
       ?
PgBouncer
       ?
Read replicas
       ?
Partitioning
       ?
Sharding where required
```

### Why Increasing `max_connections` Fails
Blindly increasing `max_connections` on PostgreSQL is **not a scaling strategy**. Each PostgreSQL connection allocates an independent backend process with its own memory overhead (`work_mem`). Having hundreds of active concurrent OS processes causes severe context switching, buffer lock contention, and cache thrashing, degrading throughput rather than improving it.

### Sharding as a Scale-Out Architecture
In this capacity planning model, high-throughput tables in `payment_db` (`payment`, `order_record`, `outbox_event`, `payment_transition_log`) are designed for horizontal sharding (using application-level sharding or Citus) partitioned by `merchant_id`. This distributes write I/O and locking overhead across multiple independent PostgreSQL nodes while isolating merchant failure domains. This sharding design is specified as a future scale-out architecture rather than a feature of the current local deployment.

---

## Database-per-Service at Scale

The platform strictly enforces the **Database-per-Service** pattern. In a 10,000 TPS deployment, each service database scales independently based on its specific workload characteristics:

```
Merchant Service
      |
Merchant DB

Payment Service
      |
Payment DB
   /       \
Writer    Readers

Operations Service
      |
Operations DB

Vault Service
      |
Vault DB
```

### Independent Database Scaling Profiles
- **Payment Database (`payment_db`)**: Carries the dominant transactional workload. Requires the most aggressive scaling strategy: a partitioned primary cluster with read replicas for dashboard reporting and order queries.
- **Operations Database (`operations_db`)**: Optimized for high write and batch I/O during settlement generation and webhook history logging.
- **Merchant Database (`merchant_db`)**: Lower write frequency (onboarding, key generation); easily handled by a primary instance with multi-AZ read replicas and aggressive caching in Redis.
- **Vault Database (`vault_db`)**: Focused on low-latency token lookups and card persistence with strict access control and read replicas.

---

## Database Connection Management

10,000 TPS **does not mean 10,000 database connections**. High-concurrency systems multiplex large numbers of application threads onto a compact pool of physical database connections.

```
Application replicas
      |
      v
HikariCP
      |
      v
PgBouncer
      |
      v
PostgreSQL
```

### Multi-Tier Connection Architecture
- **HikariCP Application Pools**: Each application replica maintains a compact connection pool (e.g., `maximum-pool-size: 15–20`). Multiplying 50 payment pods by 20 produces 1,000 client-side connections.
- **PgBouncer Pooling Layer**: A dedicated fleet of PgBouncer instances operates in **Transaction Pooling Mode**. PgBouncer holds incoming connections from application pods and assigns physical database connections only for the exact duration of a transaction block. Once the transaction commits, the physical connection is instantly returned to the pool.
- **Database Engine Stability**: PgBouncer reduces 1,000+ application connections to approximately 100–150 active physical server connections at PostgreSQL. This maintains optimal CPU core utilization and prevents database thrashing. Blindly increasing pool sizes increases contention and degrades database stability.

---

## Redis Cluster Scaling

In a 10,000 TPS environment (handling ~20,000 HTTP ingress requests/second), a single Redis instance is **not an appropriate assumption**. The single-threaded Redis event loop and network I/O interface would quickly saturate.

The capacity plan transitions Redis to a **Redis Cluster with master-replica shards**:

```mermaid
flowchart TD
    APPS["Application Pods"] --> REDIS["Redis Cluster"]
    subgraph Shards ["Cluster Shards"]
        S1["Shard 1"]
        S2["Shard 2"]
        S3["Shard 3"]
    end
    REDIS --> S1 & S2 & S3
```

### Redis Workloads at Scale
- **API-Key Verification Cache**: Ephemeral verification results (`30s TTL`) bypassing gateway BCrypt checks.
- **Idempotency Store**: Atomic `SETNX` reservation and response payload storage (`24h TTL`) preventing duplicate charge executions.
- **Distributed Rate Limiting**: Shared sliding window and token bucket counters evaluating traffic limits per API key.
- **Distributed Locks**: ShedLock coordination across multi-replica scheduled tasks.
- **API Key Metadata Cache**: Read-through cache (`5m TTL`) reducing Feign lookups to `merchant-service`.

### High Availability and Distribution
- **Hash Slot Distribution**: Redis Cluster partitions its 16,384 hash slots uniformly across 6–12 master shards. Keys use consistent hash tagging (e.g., `{keyId}`) where atomic multi-key operations are required.
- **Automatic Failover**: Each master shard is paired with a synchronized replica. Redis Cluster detects node failures and executes sub-second failover, preventing cache outages from disrupting active payment traffic.

---

## Kafka Scaling

Kafka functions as the asynchronous event backbone at high throughput, decoupling real-time payment authorization from downstream settlement, webhook delivery, and audit logging.

```mermaid
flowchart TD
    subgraph PaymentServices ["Payment Services"]
        P1["P1"]
        P2["P2"]
        P3["P3"]
        P4["P4"]
        P5["P5"]
    end
    subgraph KafkaBrokers ["Kafka Brokers"]
        B1["B1"]
        B2["B2"]
        B3["B3"]
    end
    PaymentServices --> KafkaBrokers
    KafkaBrokers --> OPS["Operations Consumers"]
```

### Kafka Scale-Out Architecture
- **Multi-Broker Cluster**: A cluster of 3–7 brokers running KRaft consensus with a replication factor of 3 to guarantee fault tolerance and high write throughput.
- **Topic Partitions**: Core topics (`payments.events`, `orders.events`) are partitioned across **32 to 64 partitions** using `merchantId` as the partition key.
- **Partition-Level Parallelism**: Partitioning ensures strict chronological event ordering for any single merchant while enabling up to 64 consumer instances to process event streams concurrently.
- **Consumer Groups & Parallel Consumers**: Downstream operations (`operations-webhook-group`, `operations-settlement-group`) consume events independently at their own processing cadence.
- **Consumer Lag Telemetry**: Prometheus continuously monitors `kafka_consumergroup_lag`. If consumer lag increases during peak surges, Kubernetes scales up consumer pod replicas to absorb the backlog.

---

## API Gateway Scaling

The API Gateway represents the critical ingress choke point because every HTTP request must traverse its authentication, rate limiting, and routing pipelines.

```
~20,000 HTTP requests/sec
          |
          v
    Load Balancer
          |
    +-----+-----+
    |     |     |
   G1    G2    G3 ...
    |
    v
Authentication
Rate Limiting
Routing
```

### Scaling Mechanisms
- **Gateway Replica Fleet**: A horizontally scaled fleet of ~30 gateway pods behind an external L4/L7 cloud load balancer (e.g., AWS NLB / ALB).
- **BCrypt Optimization Connection**: Load testing revealed that raw BCrypt verification consumed massive gateway CPU. The Redis API-key verification cache (caching verification outcomes for 30 seconds) reduced P99 latency by **~86.1% (from ~16.000s down to ~2.221s in local benchmark testing)**. In a 10,000 TPS architecture, this caching mechanism prevents the gateway fleet from requiring hundreds of compute cores solely for password hashing.
- **Stateless Routing**: Routes dispatch directly to internal Kubernetes ClusterIP services with HTTP connection pooling.

---

## Rate Limiting at Scale

At 10,000 TPS, robust rate limiting is essential to preserve system stability against runaway clients or abusive spikes:

```
10,000 TPS legitimate traffic
+
one abusive merchant
+
traffic spike
=
potential cascading overload
```

### Distributed Rate Limiting Architecture
Rate limiting must remain consistent regardless of which gateway replica receives a request:

```mermaid
flowchart TD
    LB["Load Balancer"] --> G1["G1"] & G2["G2"] & G3["G3"]
    G1 & G2 & G3 --> REDIS["Redis (Shared Rate-Limit State)"]
```

- **Shared State**: All gateway replicas evaluate limits against shared Redis data structures using atomic Lua scripts (`SlidingWindowLuaLimiter` or `TokenBucketRateLimiter`).
- **Graceful Rejection**: Requests exceeding thresholds are immediately rejected at the edge with `HTTP 429 Too Many Requests` and standard `Retry-After` headers, preventing unauthorized or excessive traffic from consuming downstream service threads or database connections.

---

## Failure Isolation at 10,000 TPS

At high traffic volume, partial failures in non-critical components can quickly cascade and bring down the entire platform:

```
Merchant Service slowdown
          |
          v
Circuit Breaker
          |
          X
Prevent cascading failure
          |
          v
Payment system remains available
where architecture allows
```

### Implemented Isolation Mechanisms
- **Resilience4j Circuit Breakers & Retries**: Active on inter-service Feign clients (e.g., `CustomerServiceClient`). If `merchant-service` degrades, the circuit breaker opens to fail fast and prevent thread pool exhaustion in `payment-service`.
- **Bulkheads & Connection Timeouts**: Strict HTTP connect (500ms) and read (2000ms) timeouts on `feign-hc5` prevent slow remote calls from holding threads indefinitely.
- **Kafka Decoupling**: Webhook delivery and merchant settlement processing are completely decoupled from payment processing. A complete outage in `operations-service` or merchant webhook endpoints has zero impact on live authorization and capture flows.
- **Transactional Outbox Resilience**: If Kafka brokers experience transient network partitions, payment transactions continue to commit locally to PostgreSQL; outbox records accumulate safely and are published once broker connectivity recovers.
- **Idempotency Safeguards**: Concurrent retries from impatient clients or network blips are intercepted by Redis and PostgreSQL unique constraints.
- **Realistic Expectation**: While these patterns dramatically limit blast radiuses, no distributed architecture can claim total, absolute failure isolation under all catastrophic conditions.

---

## Kubernetes Autoscaling (HPA)

In a cloud Kubernetes environment, Horizontal Pod Autoscalers (HPA) dynamically adjust replica counts based on observed workload demand:

```mermaid
flowchart TD
    subgraph K8s ["Kubernetes Cluster"]
        HPA_GW["Gateway HPA"]
        HPA_PAY["Payment HPA"]
        HPA_OPS["Operations HPA"]
    end
    HPA_GW --> G1["G1"] & G2["G2"] & G3["G3"]
    HPA_PAY --> P1["P1"] & P2["P2"] & P3["P3"]
    HPA_OPS --> O1["O1"] & O2["O2"]
```

### Autoscaling Signal Strategy
- **Application Signals**: Autoscaling decisions must leverage composite metrics including **HTTP request rate** (via Prometheus custom metrics adapter), **Kafka consumer lag**, and **request latency**, rather than relying solely on CPU or memory utilization.
- **Limitation Awareness**: HPA alone does **not** solve capacity planning. Adding application pods without scaling database connection pools, storage IOPS, and broker partitions simply accelerates downstream exhaustion.

---

## Observability at Scale

A 10,000 TPS distributed system cannot be operated safely without comprehensive, real-time observability:

```
Services
   |
   +--> Prometheus
   |       |
   |       v
   |     Grafana
   |
   +--> Zipkin
           |
           v
      Distributed Trace
```

### Telemetry Pipeline & Monitoring Metrics
- **Prometheus Metric Scraping**: Scrapes Micrometer endpoints across all pods. Telemetry targets include:
  - Ingress HTTP request rate and HTTP 4xx/5xx error rates
  - P95 and P99 latency per API route
  - JVM process CPU and garbage collection pause times
  - HikariCP active connections, idle connections, and connection acquisition wait times
  - Kafka consumer group lag and partition offsets
  - Redis command latency and memory utilization
- **Metric Cardinality Management**: Metrics strictly utilize bounded low-cardinality label sets (HTTP method, route pattern, status code) to prevent Prometheus memory exhaustion. High-cardinality identifiers (like `paymentId` or `customerId`) are never emitted as metric tags.
- **Distributed Trace Sampling**: Trace sampling is configured at **1–5% of requests** under peak load to provide granular end-to-end tracing in Zipkin / OpenTelemetry without generating unsustainable network I/O or storage overhead.

---

## Illustrative 10,000 TPS Capacity Plan

> [!NOTE]
> *This table represents an **illustrative 10,000 TPS capacity-planning model** for production sizing and is not the current deployment configuration. Actual sizing must be validated through distributed load testing, profiling, query analysis, and production telemetry.*

| Component | Target Scale | CPU Allocation | Memory Allocation | Purpose / Sizing Rationale |
| :--- | :---:| :---:| :---:| :--- |
| **API Gateway** | ~30 Pods | 4 vCPU per pod | 6 GiB per pod | Handles ~20,000 ingress HTTP req/sec, rate limiting, and token validation. |
| **Payment Service** | ~50 Pods | 4 vCPU per pod | 8 GiB per pod | Highest-volume business workload: state machine, idempotency, outbox writes. |
| **Merchant Service** | ~15 Pods | 2 vCPU per pod | 4 GiB per pod | Manages API key lookups, customer directory, and merchant profiles. |
| **Vault Service** | ~15 Pods | 2 vCPU per pod | 4 GiB per pod | Executes AES-256 card encryption, tokenization, and direct charging. |
| **Operations Service** | ~12 Pods | 2 vCPU per pod | 4 GiB per pod | Settlement batch processing, webhook delivery, retry queues, and DLQ. |
| **PgBouncer** | ~4 Pods | 2 vCPU per pod | 2 GiB per pod | Multiplexes thousands of application threads into a controlled physical connection pool. |
| **PostgreSQL (`payment_db`)** | 4 Shards + Replicas | 16–32 vCPU per shard | 64–128 GiB per shard | Partitioned by `merchant_id` to handle high transaction and outbox volume. |
| **PostgreSQL (Other DBs)** | Primary + Replicas | 8–16 vCPU per DB | 32–64 GiB per DB | Independent databases for `merchant_db`, `vault_db`, and `operations_db`. |
| **Redis Cluster** | 6–12 Shards (M/R) | Dedicated nodes | 16–32 GiB per node | Distributes rate-limit keys, idempotency entries (24h TTL), and auth caches. |
| **Apache Kafka** | 5–7 Brokers | 8 vCPU per broker | 32 GiB per broker | Delivers partitioned, high-throughput event streaming with replication factor 3. |

---

## Architecture Comparison: Current vs. 10k TPS Target

The system design explicitly distinguishes three concepts:
1. **Current Implementation**: The codebase, microservice boundaries, schemas, and configurations that actually exist in this repository.
2. **Current Load-Test Results**: The empirical measurements obtained during synthetic load tests on the single-node Kind Kubernetes cluster (100–300 req/sec).
3. **10,000 TPS Capacity Plan**: The hypothetical target architecture, horizontal scaling model, and infrastructure sizing required to operate at production scale.

| Layer | Current Project | 10k TPS Architecture |
| :--- | :--- | :--- |
| **Services** | Horizontally deployable | Many replicas + autoscaling (HPA) |
| **Gateway** | Kubernetes deployment | Load-balanced gateway fleet (~30 pods) |
| **Database** | PostgreSQL per service | Pooling (PgBouncer) + replicas + sharding where required |
| **Redis** | Redis-backed mechanisms | Redis Cluster (6–12 shards, master-replica) |
| **Kafka** | Event bus (single-node KRaft) | Multi-broker cluster (5–7 brokers, 32–64 partitions) |
| **Kubernetes** | Kind / local deployment | Production cloud Kubernetes (AWS EKS / GCP GKE) |
| **Observability** | Standalone Prometheus, Grafana & Zipkin | Production HA observability (Thanos / OpenTelemetry) |
| **Load Testing** | Local synthetic load testing (100–300 req/sec) | Distributed load generators (distributed JMeter / k6) |

---

## Infrastructure Cost Considerations

Planning a 10,000 TPS architecture requires cost awareness and realistic cloud pricing considerations:
- **Illustrative Nature**: All figures are architectural planning estimates and will vary based on cloud provider, region, reserved instances vs. on-demand pricing, and managed vs. self-managed deployments. They do not represent a guaranteed billing invoice.
- **Compute-Only Estimate**: Provisioning ~120 compute pods (300+ vCPUs, 600+ GiB RAM) across managed cloud Kubernetes nodes typically runs between **$8,000 and $14,000 per month** in pure compute capacity (e.g., AWS EC2 on-demand/savings plans).
- **Realistic All-In Infrastructure Cost**: When factoring in managed multi-shard PostgreSQL clusters with provisioned IOPS (AWS Aurora / RDS), managed multi-node Redis clusters (ElastiCache), managed Kafka (AWS MSK / Confluent Cloud), cross-AZ network egress bandwidth, cloud load balancers, and observability ingestion and retention (Datadog or self-hosted Thanos/Loki storage), the **realistic all-in cost ranges from $15,000 to $25,000+ per month**.
- **Architectural Efficiency Impact**: The API-key verification cache optimization directly impacts these costs: by reducing gateway CPU overhead by over 80%, the fleet requires significantly fewer gateway pods to sustain peak ingress traffic.

---

## Target 10,000 TPS System Architecture Diagram

```mermaid
flowchart TB
    INTERNET["Public Internet Traffic (~20,000 HTTP req/sec)"] --> EXT_LB["Cloud Load Balancer (AWS NLB / Cloudflare)"]

    subgraph EdgeFleet ["Edge Ingress Fleet (30+ Replicas)"]
        GW_F["API Gateway Fleet (Spring Cloud Gateway WebMVC)\n• JWT & Basic Auth Validation\n• In-Memory Header Injection\n• Short-Lived Verification Cache (30s)"]
    end

    subgraph ServiceFleet ["Horizontally Scaled Microservice Fleet"]
        MS_F["merchant-service Fleet (15 Replicas)"]
        PS_F["payment-service Fleet (50 Replicas)"]
        VS_F["vault-service Fleet (15 Replicas)"]
        OS_F["operations-service Fleet (12 Replicas)"]
    end

    subgraph SharedDataInfra ["Shared Distributed Data & Coordination Infrastructure"]
        RD_CLUST[("Redis Cluster (Sharded)\n• Distributed Rate Limits\n• Idempotency Store (24h TTL)\n• API Key Secret Cache")]
        
        PGB_POOL["PgBouncer Connection Pooling Fleet"]
        
        subgraph PostgresCluster ["Partitioned PostgreSQL Databases"]
            PG_PAY[("payment_db (Sharded by merchantId)\nPrimary Writers + Read Replicas")]
            PG_OTHER[("merchant_db, vault_db, operations_db\nPrimary + Multi-AZ Read Replicas")]
        end
        
        KF_CLUST{{"Apache Kafka Multi-Broker Cluster (5-7 Brokers)\nTopics partitioned across 32-64 partitions"}}
    end

    subgraph ObservabilityMesh ["Centralized Observability Infrastructure"]
        PROM_HA["Prometheus HA / Thanos Cluster"]
        GRAF_HA["Grafana Dashboards"]
        ZIP_HA["Distributed Tracing (OpenTelemetry / Zipkin)"]
    end

    EXT_LB --> GW_F
    GW_F <--> RD_CLUST
    GW_F --> MS_F & PS_F & VS_F & OS_F

    PS_F --> PGB_POOL
    PGB_POOL --> PG_PAY
    MS_F & VS_F & OS_F --> PG_OTHER

    PS_F -- Transactional Outbox Poller --> KF_CLUST
    KF_CLUST --> OS_F

    ServiceFleet & EdgeFleet -.-> PROM_HA --> GRAF_HA
    ServiceFleet & EdgeFleet -.-> ZIP_HA
```

---

## Final Project Status

This repository represents the **completed portfolio implementation** of the RazorPay-inspired distributed payment gateway. The system demonstrates distributed payment processing, microservice decomposition, event-driven communication, transactional consistency, idempotency, rate limiting, sensitive-data isolation, Kubernetes deployment, observability, and measured performance optimization.

> [!NOTE]
> The **Scaling the Architecture to 10,000 TPS** section is a **capacity-planning and system-design exercise**, not a claim about the throughput of the current local deployment. The project includes a 10,000 TPS capacity-planning model that estimates the horizontal scaling, database architecture, Kafka capacity, Redis clustering, connection pooling, and Kubernetes resources required to target that workload.
