# Integration Tests (E2E)

End-to-end test suite for the Employee CRUD Event-Driven System using **Cucumber** (BDD) and **REST Assured**.

## Roles & Responsibilities

- Validates end-to-end workflows across all microservices
- Tests employee CRUD flows (producer → REST API)
- Tests user management CRUD flows (users-service)
- Tests authentication and authorization (OAuth2 JWT + RBAC)
- Validates error handling (400, 401, 403, 404)

## Technology Stack

- **Framework**: Cucumber 7.x (Gherkin / BDD)
- **HTTP Client**: REST Assured 5.x
- **Test Runner**: JUnit Platform Suite
- **Build**: Maven Failsafe Plugin (profile: `e2e`)

## Prerequisites

Ensure the full system is running:

```bash
cd docker && docker compose -f keycloak-compose.yml up -d
```

Then start all three services (in separate terminals):

```bash
# Terminal 1 - Employee Service
cd employee-service && ../mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 2 - Employee Consumer
cd employee-service-consumer && ../mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 3 - Users Service
cd users-service && ../mvnw spring-boot:run
```

## Running Tests

```bash
# Build the module (installs deps)
./mvnw clean install -pl integration-tests -am

# Run the e2e tests
./mvnw verify -pl integration-tests -Pe2e
```

## Feature Files

| Feature File | Scenarios | Coverage |
|---|---|---|
| `employee/employee_crud.feature` | 5 | Create, Get, List, Update, Delete |
| `employee/employee_errors.feature` | 3 | Validation error (400), Not Found (404), Unauthorized (401) |
| `users/users_crud.feature` | 6 | Create, Get by ID, Get by username, List, Update, Delete |
| `users/users_auth.feature` | 2 | Forbidden (403), Unauthorized (401) |

## Configuration

Service URLs and Keycloak credentials are configured in `src/test/resources/application.yml`:

```yaml
e2e:
  employee-service:
    base-url: http://localhost:8083
  employee-consumer:
    base-url: http://localhost:8082
  users-service:
    base-url: http://localhost:8084
  keycloak:
    token-url: http://localhost:8081/realms/dev/protocol/openid-connect/token
    client-id: newClient
    client-secret: newClientSecret
```

## Test Users

| User | Password | Roles | Can manage users |
|---|---|---|---|
| `john@test.com` | `123` | `manage-users`, `view-users`, `query-users` | Yes |
| `mike@other.com` | `123` | basic | No (403) |
