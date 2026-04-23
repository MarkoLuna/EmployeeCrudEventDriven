# Employee Service Consumer

The **Employee Service Consumer** is a critical component of the event-driven Employee Management System. It is responsible for processing asynchronous events from Kafka and ensuring data persistence.

## Roles & Responsibilities
- **Event Consumption**: Listens to `employee-upsert.v1` and `employee-deletion.v1` topics.
- **Data Persistence**: Manages employee records in **MongoDB**.
- **Internal API**: Exposes read-only REST endpoints used by the *Employee Service (Producer)* to fetch data for GET and LIST requests.
- **OAuth2 Security**: Acts as a Resource Server, validating JWT tokens issued by Keycloak.

## Technology Stack
- **Framework**: Spring Boot 3.4.4
- **Persistence**: Spring Data MongoDB
- **Messaging**: Spring Kafka
- **Security**: Spring Security OAuth2 Resource Server
- **Documentation**: Swagger UI (OpenAPI)

## Getting Started

### Prerequisites
Ensure the infrastructure (Kafka, MongoDB, Keycloak) is running. Refer to the [Root README](../README.md) for full setup instructions.

### Running the Application
You can run the consumer in two modes:

#### 1. Docker Mode (Recommended)
This is the default mode when running the entire system via Docker Compose.
```bash
# From the root directory
docker compose -f docker/keycloak-compose.yml up -d employee-service-consumer
```

#### 2. Local Development Mode
Use this mode if you want to run the consumer from your IDE or terminal against the Docker infrastructure.
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Kafka Topics
| Topic Name | Purpose |
| :--- | :--- |
| `employee-upsert.v1` | Receives employee creation and update events. |
| `employee-deletion.v1` | Receives employee deletion events. |

## Useful Endpoints
- **Health Check**: `http://localhost:8082/actuator/health`
- **Swagger UI**: `http://localhost:8082/swagger-ui/index.html`
- **Internal API**: `http://localhost:8082/internal/employees` (Requires valid JWT)

## Code Quality
### Spotless (Formatting)
```bash
mvn spotless:check
mvn spotless:apply
```

### Version Updates
```bash
mvn versions:display-property-updates 
```
