# Employee CRUD Event-Driven System

An event-driven microservices application for managing employee records, featuring asynchronous processing, OAuth2 security, multi-database persistence, and resilient inter-service communication.

## Table of Contents
- [Introduction](#introduction)
- [Architecture](#architecture)
- [Fault Tolerance](#fault-tolerance)
- [Project Structure](#project-structure)
- [Requirements](#requirements)
- [How to Run Locally](#how-to-run-locally)
- [Process Flows](#process-flows)
- [User Management](#user-management)
- [API Testing (Bruno)](#api-testing-bruno)
- [Technologies Used](#technologies-used)

---

## Introduction
This project demonstrates a modern, scalable approach to CRUD operations using an **Event-Driven Architecture (EDA)**. Instead of traditional synchronous persistence, the system decouples operations via **Apache Kafka**, ensuring high availability and system resilience. Inter-service communication is hardened with **Resilience4j** circuit breakers, timeouts, and retry mechanisms.

---

## Architecture
The system is composed of several decoupled components:

1.  **Employee Service (Producer)**:
    - Exposes REST APIs for Employee management.
    - **Read Operations**: Queries the Employee Service Consumer via REST (Feign) for GET and LIST requests, protected by a **Resilience4j Circuit Breaker** (`employeeConsumer`) with fallback to empty pages.
    - **Write Operations**: Validates requests and publishes events to Kafka for CREATE, UPDATE, and DELETE.
    - Acts as an OAuth2 Resource Server.
    - Feign client configured with **5s connect / 10s read timeouts** and a **method-aware retryer** (GET-only, up to 3 attempts).
2.  **Employee Service Consumer (Consumer)**:
    - Listens to Kafka topics (`employee-upsert.v1`, `employee-deletion.v1`).
    - Handles data persistence and background tasks.
    - **Non-blocking retries** (max 3, 5m backoff) for transient failures, with **DLT persistence** to MongoDB `dead_letter_queue` collection for unrecoverable messages.
    - **Data Access**: Exposes REST endpoints used by the Producer for read operations.
3.  **Users Service**:
    - Dedicated microservice for user management.
    - Exposes REST APIs for user CRUD operations.
    - Communicates with IAM Service (Keycloak) via Feign client, protected by a **Resilience4j Circuit Breaker** (`keycloak`) with fallback to `503 Service Unavailable`.
    - Acts as an OAuth2 Resource Server.
    - Feign client configured with **5s connect / 10s read timeouts** and a **method-aware retryer** (GET-only, up to 3 attempts).
4.  **Employee API**:
    - A shared module providing common DTOs, interfaces, and utility classes used by both services.
5.  **Infrastructure**:
    - **Kafka & Zookeeper**: Message broker for asynchronous event delivery.
    - **Keycloak**: Centralized Identity and Access Management (IAM).
    - **MongoDB & PostgreSQL**: Used for persistent storage.
    - **Nginx**: Reverse proxy with 5s connect timeout, 30s read timeout, and `proxy_next_upstream` retry on 5xx errors.

---

## Fault Tolerance

The system implements a layered fault tolerance strategy across all inter-service communication channels.

### Resilience4j Circuit Breakers

| Instance | Service | Target | Fallback Behavior |
|---|---|---|---|
| `employeeConsumer` | Employee Service | Consumer Feign (reads) | Returns empty page on LIST; throws `503` on GET |
| `keycloak` | Users Service | Keycloak Feign (all ops) | Returns empty page on LIST; throws `503` on all others |

### Circuit Breaker Config (default)
- **Sliding window**: 10 calls
- **Failure threshold**: 50%
- **Wait duration (open → half-open)**: 30s
- **Half-open calls**: 3
- **Recorded exceptions**: `EmployeeServiceConsumerException`, `TimeoutException`, `FeignException`

### Feign Client Resilience
- **Connect timeout**: 5s
- **Read timeout**: 10s
- **Retryer**: GET-only, up to 3 attempts (100ms base, 1s max period)

### Kafka Consumer Resilience
- **Non-blocking retry**: max 3 attempts, 5m fixed backoff
- **Retryable exceptions**: `RetryableMessagingException` only
- **Dead letter topic**: Messages exceeding max retries are sent to DLT
- **DLT persistence**: Failed messages are persisted to MongoDB `dead_letter_queue` collection for manual inspection

### Nginx Reverse Proxy Resilience
- **Connect timeout**: 5s
- **Read timeout**: 30s
- **Upstream retry**: On `error`, `timeout`, `502`, `503`, `504` (up to 3 attempts)

### Sequence Diagram: Fault Tolerance Flow

```mermaid
sequenceDiagram
    participant User
    participant Producer as Employee Service
    participant CB as Circuit Breaker
    participant Consumer as Employee Service Consumer

    User->>Producer: GET /employees

    Note over Producer,CB: Normal flow
    Producer->>CB: listEmployees()
    CB->>Consumer: Feign call
    Consumer-->>CB: 200 OK
    CB-->>Producer: EmployeePage
    Producer-->>User: 200 OK

    Note over Producer,CB: Circuit open / timeout
    User->>Producer: GET /employees
    Producer->>CB: listEmployees()
    Note over CB: Circuit OPEN or timeout
    CB-->>Producer: Fallback triggered
    Producer-->>User: 200 OK (empty page)
```
```text
.
├── docker/                      # Infrastructure configuration (Docker Compose, Keycloak, Nginx)
│   ├── keycloak-compose.yml     # Main infrastructure definition
│   └── ...
├── employee-api/                # Shared module (DTOs, Common Logic)
├── employee-service/            # Producer service (REST API + Kafka Producer)
├── employee-service-consumer/   # Consumer service (Kafka Consumer + Persistence)
├── users-service/              # Users service (User Management + IAM Integration)
├── integration-tests/           # E2E test suite (Cucumber + REST Assured)
├── employee-crud-frontend/       # React SPA (employee & user management UI)
├── .bruno/                      # Bruno API collection for testing
├── pom.xml                      # Root Maven configuration
└── README.md                    # Project documentation
```

---

## Requirements
- **Java 21**
- **Maven 3.8+**
- **Node.js 20+** (required for the frontend — see `employee-crud-frontend/README.md`)
- **Docker & Docker Compose**
- **Hosts File**: Add the following entry to your `/etc/hosts`:
  ```text
  127.0.0.1 localstack.lks.com
  ```

---

## How to Run Locally

### 1. Start Infrastructure
Launch the required services (Kafka, Mongo, Postgres, Keycloak) using Docker Compose:
```bash
cd docker
docker compose -f keycloak-compose.yml up -d
```

### 2. Build the Project
Compile and install all modules from the root directory:
```bash
./mvnw clean install
```

### 3. Run the Services
Open three terminals and run the following:

**Terminal 1: Employee Service**
```bash
cd employee-service
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**Terminal 2: Employee Consumer**
```bash
cd employee-service-consumer
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**Terminal 3: Users Service**
```bash
cd users-service
./mvnw spring-boot:run
```

### Default Users

The following users are pre-configured in the `dev` realm:

- **John Doe** (`john@test.com` / `123`): Has **`manage-users`**, **`view-users`**, **`query-users`** client roles in realm-management. Authorized to perform all user management and employee CRUD operations.
- **Mike Smith** (`mike@other.com` / `123`): Has only **`account`** client roles. Authorized for employee operations but restricted from user management.

---

## End-to-End Testing

The project includes a **Cucumber**-based E2E test suite that covers employee and user management workflows against running services.

### Prerequisites
Ensure the full system is running (infra + all 3 services) as described in [How to Run Locally](#how-to-run-locally).

### Run
```bash
./mvnw verify -pl integration-tests -Pe2e
```

### Test Coverage
- **Employee CRUD**: Create, retrieve, list, update, delete
- **Employee Error Handling**: Validation errors (400), not found (404), unauthorized (401)
- **User CRUD**: Create, retrieve by ID/username, list, update, delete
- **User Authorization**: RBAC enforcement — forbidden for insufficient roles (403), unauthorized without token (401)

### Feature Files
Located in `integration-tests/src/test/resources/features/`. See [integration-tests/README.md](integration-tests/README.md) for details.

---

## Process Flows

### Read Operations (GET/LIST)
```mermaid
sequenceDiagram
    participant User
    participant Producer as Employee Service
    participant CB as Circuit Breaker
    participant Consumer as Employee Service Consumer
    participant DB as MongoDB/PostgreSQL

    User->>Producer: GET /employees/{id}
    Producer->>CB: @CircuitBreaker list()/getEmployee()
    CB->>Consumer: Feign call (5s connect / 10s read)
    Critical Section
      Consumer->>DB: Fetch Data
      DB-->>Consumer: Employee Data
      Consumer-->>CB: Employee DTO
    option Failure / Timeout
      CB-->>Producer: Fallback method invoked
      Producer-->>User: 200 OK (empty) or 503
    end
    CB-->>Producer: Employee DTO
    Producer-->>User: 200 OK
```

### Employee Upsert Flow (Create/Update)
```mermaid
sequenceDiagram
    participant User
    participant Service as Employee Service
    participant Kafka as Kafka (employee-upsert.v1)
    participant Consumer as Employee Service Consumer
    participant DB as MongoDB/PostgreSQL

    User->>Service: POST/PUT /employees
    Note over Service: Validate & Map DTO
    Service->>Kafka: Publish EmployeeUpsertEvent
    Service-->>User: 202 Accepted / 200 OK
    Kafka->>Consumer: Consume Upsert Event
    Consumer->>DB: Persist Employee Data
    Consumer-->>Consumer: Log Success
```

### Employee Deletion Flow
```mermaid
sequenceDiagram
    participant User
    participant Service as Employee Service
    participant Kafka as Kafka (employee-deletion.v1)
    participant Consumer as Employee Service Consumer
    participant DB as MongoDB/PostgreSQL

    User->>Service: DELETE /employees/{id}
    Service->>Kafka: Publish EmployeeDeletionEvent
    Service-->>User: 204 No Content
    Kafka->>Consumer: Consume Deletion Event
    Consumer->>DB: Remove Employee Data
    Consumer-->>Consumer: Log Success
```

---

## User Management

The Users Service provides dedicated user management capabilities that integrate with the IAM Service (Keycloak) for centralized identity and access management.

### User Endpoints

The Users Service exposes the following user management endpoints under `/users`:

- **POST /users** - Create a new user
- **GET /users/{id}** - Get user by ID
- **GET /users/username/{username}** - Get user by username
- **GET /users/{page}/{size}** - Get all users with pagination
- **PUT /users/{id}** - Update user
- **DELETE /users/{id}** - Delete user

The IAM Service provides endpoints for user management (require OAuth2 authentication):

- **POST /api/users** - Create a new user
- **GET /api/users/{page}/{size}** - List all users with pagination
- **GET /api/users/{id}** - Get user by ID
- **GET /api/users/username/{username}** - Get user by username
- **PUT /api/users/{id}** - Update user details and roles
- **DELETE /api/users/{id}** - Delete a user

### Role-Based Access Control (RBAC)

All user management endpoints are protected with role-based access control using Spring Security's `@PreAuthorize` annotations. The following roles are required:

| Operation | Required Roles |
|-----------|----------------|
| Create User | `admin`, `manage-users` |
| Get User by ID | `admin`, `manage-users`, `view-users`, `query-users` |
| Get User by Username | `admin`, `manage-users`, `view-users`, `query-users` |
| Get All Users | `admin`, `manage-users`, `view-users`, `query-users` |
| Update User | `admin`, `manage-users` |
| Delete User | `admin`, `manage-users` |

### Keycloak Integration

The user management functionality communicates with the IAM Service (Keycloak) via Feign client. The IAM Service handles:

- User creation and deletion in Keycloak
- User attribute management
- Role assignment (realm roles and client roles)
- Email verification status
- User enable/disable operations

### Configuration

The IAM Service base URL is configured in `application.yml`:

```yaml
services:
  iam-service:
    base-url: http://localhost:8081
```

The Users Service runs on port 8084 by default.

### Data Synchronization

When users are created, updated, or deleted through the Users Service:
1. The request is validated and processed
2. The Feign client communicates with the IAM Service
3. The IAM Service performs the actual operations in Keycloak
4. User data is synchronized between Keycloak and the application

### Security

All user endpoints require:
- Bearer token authentication (JWT from Keycloak)
- Appropriate role assignments as specified in the RBAC table above
- OAuth2 Resource Server configuration

---

## API Testing (Bruno)

The project includes a [Bruno](https://www.usebruno.com/) collection for testing the API endpoints.

### Setup
1.  Install the **Bruno** API client.
2.  Open Bruno and select **Open Collection**.
3.  Navigate to the `.bruno/` directory in this project.
4.  Select the `local` environment from the environment dropdown to set the base URL and authentication variables.

### Available Requests
- **Auth**: `GetToken`, `GetToken User 2`
- **Employee CRUD**: `ListEmployees`, `GetEmployee`, `CreateEmployee`, `UpdateEmployee`, `DeleteEmployee`
- **User Management**: `CreateUser`, `GetUserById`, `GetUserByUsername`, `GetAllUsers`, `UpdateUser`, `DeleteUser`
- **Health**: `health`

---

## Frontend Application

A React SPA is available at [`employee-crud-frontend/`](employee-crud-frontend/) for interacting with the backend services.

### Features
- OAuth2 login with Keycloak (password grant flow)
- Employee CRUD (create, list, view, edit, delete)
- User CRUD with role-based visibility (hidden for users without realm-management roles)
- Profile page showing the logged-in user's info and roles
- Responsive layout with navigation sidebar, header (user dropdown), and footer
- Automatic token refresh via Axios interceptor

### Quick Start (Frontend)
```bash
cd employee-crud-frontend
npm install
cp .env.local .env      # or cp .env.docker .env
npm run dev              # http://localhost:5173
```

### Tech Stack
- React 19 + Vite 6 + TypeScript 5
- Tailwind CSS 4 + Lucide React icons
- React Router 6 + Axios
- Vitest + React Testing Library

See [`employee-crud-frontend/README.md`](employee-crud-frontend/README.md) for full documentation.

---

## Technologies Used
- **Backend**: Spring Boot 3.4.4, Java 21
- **Security**: Keycloak (OAuth2, OpenID Connect, JWT)
- **Messaging**: Apache Kafka & Zookeeper
- **Persistence**: MongoDB, PostgreSQL (Spring Data JPA)
- **Fault Tolerance**: Resilience4j 2.3.0 (Circuit Breaker, Retry, TimeLimiter), Spring Retry
- **API Documentation**: SpringDoc OpenAPI (Swagger)
- **Testing**: Bruno API Client, JUnit 5, Cucumber 7, REST Assured
- **Frontend**: React 19, Vite 6, TypeScript 5, Tailwind CSS 4, React Router 6, Axios
- **Infrastructure**: Docker, Nginx
