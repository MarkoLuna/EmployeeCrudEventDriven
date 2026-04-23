# Employee Service (Producer)

The **Employee Service** is the entry point for all user interactions in the Employee Management System. It provides a RESTful API for CRUD operations and orchestrates the event-driven workflow.

## Roles & Responsibilities
- **API Gateway**: Exposes public REST endpoints for managing employees.
- **Event Producer**: Publishes `EmployeeUpsertEvent` and `EmployeeDeletionEvent` to Kafka.
- **Read-Through Architecture**: Proxies GET and LIST requests to the *Employee Service Consumer* via Feign Client.
- **OAuth2 Security**: Secures all endpoints using Keycloak JWT validation.

## Technology Stack
- **Framework**: Spring Boot 3.4.4
- **Messaging**: Spring Kafka (Producer)
- **Client**: Spring Cloud OpenFeign
- **Security**: Spring Security OAuth2 Resource Server
- **Documentation**: SpringDoc OpenAPI (Swagger UI)

## Getting Started

### Prerequisites
Ensure the infrastructure (Kafka, PostgreSQL, Keycloak) is running. Refer to the [Root README](../README.md) for full setup instructions.

### Running the Application
You can run the producer in two modes:

#### 1. Docker Mode (Recommended)
```bash
# From the root directory
docker compose -f docker/keycloak-compose.yml up -d employee-service
```

#### 2. Local Development Mode
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Useful Endpoints
- **Health Check**: `http://localhost:8083/actuator/health`
- **Swagger UI**: `http://localhost:8083/swagger-ui/index.html`
- **Public API**: `http://localhost:8083/employees` (Requires valid JWT)

## API Testing
Use the [Bruno API Collection](../.bruno/) located in the root directory for testing all endpoints, including authentication flows.

## Code Quality
### Spotless (Formatting)
```bash
mvn spotless:check
mvn spotless:apply
```
