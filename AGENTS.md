# AGENTS.md — EmployeeCrudEventDriven

## Build & Format

```bash
./mvnw clean install              # Full build (all 4 modules)
./mvnw spotless:check             # Check Google Java Style (Spotless)
./mvnw spotless:apply             # Auto-fix formatting
./mvnw versions:display-property-updates  # Check dependency updates
```
- Maven enforcer auto-runs: requires Java >=21, Maven >=3.3, dependency convergence.
- Run single module: `./mvnw clean install -pl employee-service -am` (includes deps).

## Technology Stack & Versions

- **Core**: Java 21 (LTS), Spring Boot 3.4.4, Spring Cloud 2024.0.x, Maven
- **Security**: Keycloak (v26.1.2) on management port 9000, OAuth2/OIDC/JWT, Spring Security 6.4.4
- **Databases & Messaging**: Apache Kafka, PostgreSQL (Keycloak/internal), MongoDB, Spring Data JPA & MongoDB
- **APIs & Docs**: OpenAPI 3/Swagger UI (`springdoc-openapi-starter-webmvc-ui` v2.8.6), Feign
- **Infra & Tooling**: Docker & Docker Compose, Nginx, Bruno (`EmployeeCrud/` directory)

## Architecture (CQRS-lite + EDA)

| Service | Role | Port (local) | Port (docker) |
|---|---|---|---|
| `employee-service` | REST API + Kafka producer (writes) | 8083 | 8080 |
| `employee-service-consumer` | Kafka consumer + MongoDB persistence (reads) | 8082 | 8080 |
| `users-service` | Keycloak admin proxy (Feign) | 8084 | — |
| `employee-api` | Shared DTOs, enums, exceptions (JAR dep) | — | — |

- **Writes** (POST/PUT/DELETE): producer validates → publishes to Kafka → returns 202 → consumer persists.
- **Reads** (GET/LIST): producer calls consumer synchronously via Feign (JWT forwarded).
- **EDA & Idempotency**: Use EDA for state changes. Consumer event handlers must be idempotent to prevent duplicate processing.
- **Kafka Topics**: Named `<resource>-<action>.<version>` (e.g., `employee-upsert.v1`, `employee-deletion.v1`). 1 partition, 1 replica.
- **Kafka Headers**: Include correlation IDs and timestamps in Kafka headers for traceability.
- **Consumer Retry**: Uses non-blocking retry (max 3, 5m backoff) with DLT; retries only `RetryableMessagingException`.

## Run Locally

1. **Infra**: `cd docker && docker compose -f keycloak-compose.yml up -d` (Kafka, Mongo, Keycloak, Postgres, Nginx)
2. **Build**: `./mvnw clean install`
3. **Services** (3 terminals):
   ```bash
   # Terminal 1: producer
   cd employee-service && ../mvnw spring-boot:run -Dspring-boot.run.profiles=local

   # Terminal 2: consumer
   cd employee-service-consumer && ../mvnw spring-boot:run -Dspring-boot.run.profiles=local

   # Terminal 3: users-service (no profile needed)
   cd users-service && ../mvnw spring-boot:run
   ```
- Add `127.0.0.1 localstack.lks.com` to `/etc/hosts`.
- Test users: `john@test.com`/`123` (manage-users+view-users), `mike@other.com`/`123` (basic).

## Security

- All services are OAuth2 Resource Servers (JWT from Keycloak `http://localhost:8081/realms/dev`).
- Endpoints `/**` require auth; Swagger UI, Actuator, `/error` are open.
- users-service additionally uses `@PreAuthorize` for role-based access.
- Feign clients forward the incoming Bearer token via `AuthorizationInterceptor`.

## Testing

- **Unit tests** use JUnit 5 + Mockito + AssertJ + Spring MockMvc.
- **@ActiveProfiles("test")** — test yamls set JWT issuer and disable Kafka consumer.
- **Integration tests** (under `it/` packages) are `@Disabled` — need running infra.
- **Context-load tests** (`EmployeeCrudApplicationTests`) are `@Disabled`.
- Controller tests use `standaloneSetup` with `@WithMockUser` or `with(jwt())`.

## Notable Conventions

- **DDD & API Boundaries**: Keep business logic in services/domains, and use DTOs for API boundaries.
- **Lombok**: `@Data`, `@Builder`, `@RequiredArgsConstructor` throughout.
- **Mappers**: Hand-written implementations (no MapStruct), prefixed `@Component`.
- **Logging**: Log4j2 (excludes Spring Boot default logging), with ELK profile for TCP socket to logstash:5044.
- **Virtual threads**: Enabled on producer (`spring.threads.virtual.enabled: true`).
- **Error handling**: Custom exceptions (e.g., `EmployeeNotFound`) handled via `@RestControllerAdvice`, communicating errors via `app-context-error` response header.
- **AssertJ assertions**: Auto-generated from DTOs via `assertj-assertions-generator-maven-plugin` in `employee-api`.
- **Git & Documentation**: Use descriptive commit messages. Always update documentation (README, diagrams) when changing API contracts or infrastructure.

## Quirks & Known Issues

- **No CI/CD** — `.github/` only contains java-upgrade hooks, no workflows.
- **users-service now in docker-compose** — defined in both `keycloak-compose.yml` and `keycloak-compose-full.yml` (service name `users-service`, port 8084).
- **Spring Boot version mismatch**: Parent POM is 3.5.14, but properties override to 3.4.4.
- **Typo**: `openfeing.version` (missing 'i' in feign) is consistent across all POMs.
- **Retry topics must exist**: `doNotAutoCreateRetryTopics=true`, so retry/DLT topics must be pre-created.
- **MongoDB** via Spring Data JPA annotations (`@Entity`, `@Id`) — not a reactive stack.
- **`--import-realm` overwrites user passwords**: `dev-users-0.json` has PBKDF2 hashes that don't match password `"123"`. On every `docker compose down/up`, `--import-realm` reimports the file and resets `mike@other.com`'s password to the wrong hash. Temp workaround: reset via admin API after restart. Permanent fix: replace hashes in `dev-users-0.json` with correctly generated ones for `"123"`, or remove `--import-realm` and implement conditional import.

## Existing Guidelines

- `TODO.md` — future ideas (MapStruct, GraalVM, GraphQL, etc.).
