## Compose Configurations

This directory provides two main Docker Compose configurations:

1.  **Standard ([keycloak-compose.yml](keycloak-compose.yml))**: Runs the core infrastructure (Kafka, Mongo, Postgres, Keycloak, Nginx) and the Microservices (`employee-service`, `employee-service-consumer`).
2.  **Full ([keycloak-compose-full.yml](keycloak-compose-full.yml))**: Includes everything in the Standard configuration plus the **ELK Stack** (Elasticsearch, Logstash, Kibana) for centralized logging.

---

## Services Overview

| Service | Technology | Port | Purpose |
| :--- | :--- | :--- | :--- |
| **IAM** | Keycloak | `8081` | Identity and Access Management (OAuth2/OIDC) |
| **Identity DB** | PostgreSQL | `5432` | Persistence for Keycloak users and realms |
| **Data Store** | MongoDB | `27017` | Persistent storage for the Employee Consumer service |
| **Message Broker**| Kafka | `9092` | Asynchronous event delivery |
| **Coordinator** | Zookeeper | `2181` | Distributed coordination for Kafka |
| **Reverse Proxy** | Nginx | `8080` | Entry point for services and authentication |
| **Producer** | Spring Boot | `8083` | Employee Service (REST API + Producer) |
| **Consumer** | Spring Boot | `8082` | Employee Consumer Service (Persistence) |

### ELK Stack (Full Version Only)
| Service | Technology | Port | Purpose |
| :--- | :--- | :--- | :--- |
| **Search Engine**| Elasticsearch| `9200` | Log indexing and storage |
| **Visualizer** | Kibana | `5601` | Log analysis and dashboarding |
| **Log Shipper** | Logstash | `5044` | Log collection and processing |

---

## Prerequisites

### 1. Configure Hosts File
To allow proper resolution of services (especially for Keycloak redirects and Nginx proxying), add the following entry to your `/etc/hosts`:

```text
127.0.0.1 localstack.lks.com
```

### 2. Environment Variables
The configuration relies on the [.env](.env) file. You can modify versions and credentials there. Default credentials are set to `dev`/`dev`.

---

## Getting Started

### Start Standard Infrastructure
Run the following command to start core dependencies and microservices:
```bash
docker compose -f keycloak-compose.yml up -d
```

### Start Full Infrastructure (with ELK)
Run the following command to start everything including the logging stack:
```bash
docker compose -f keycloak-compose-full.yml up -d
```

### Stop Infrastructure
To stop and remove all containers:
```bash
# Standard
docker compose -f keycloak-compose.yml down

# Full
docker compose -f keycloak-compose-full.yml down
```

### View Logs
```bash
docker compose -f [file-name].yml logs -f [service_name]
```

---

## Keycloak Configuration

### Admin Console
- **URL**: [http://localstack.lks.com:8081/admin](http://localstack.lks.com:8081/admin)
- **Username**: `dev` (from .env)
- **Password**: `dev` (from .env)

### Realm Import
The system is configured to automatically import the `dev` realm from `./keycloak/import/dev-realm` on startup.

### Generating Access Tokens

You can use the following `curl` commands to obtain JWT tokens for testing the APIs.

#### Get Master Admin Token
```bash
curl --location 'http://localhost:8081/realms/master/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'client_id=admin-cli' \
--data-urlencode 'username=dev' \
--data-urlencode 'password=dev' \
--data-urlencode 'grant_type=password'
```

#### Get Dev Realm Token (User: John)
```bash
curl --location 'http://localhost:8081/realms/dev/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'client_id=newClient' \
--data-urlencode 'client_secret=newClientSecret' \
--data-urlencode 'username=john@test.com' \
--data-urlencode 'password=123' \
--data-urlencode 'grant_type=password'
```

#### Get Dev Realm Token (User: Mike)
```bash
curl --location 'http://localhost:8081/realms/dev/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'client_id=newClient' \
--data-urlencode 'client_secret=newClientSecret' \
--data-urlencode 'username=mike@other.com' \
--data-urlencode 'password=pass' \
--data-urlencode 'grant_type=password'
```

---

## Nginx Proxy Paths

The Nginx proxy (listening on port `8080`) provides the following routes:

- `/auth/` -> Proxies to Keycloak (`8081`)
- `/service/` -> Proxies to Employee Service (`8083`)
- `/consumer/` -> Proxies to Employee Service Consumer (`8082`)

*Note: Access them via `http://localstack.lks.com:8080/[path]`.*

---

## ELK Stack Details

When running the **Full** configuration, you can access the ELK stack at the following URLs:

- **Elasticsearch**: [http://localstack.lks.com:9200](http://localstack.lks.com:9200)
- **Kibana**: [http://localstack.lks.com:5601/app/home#/](http://localstack.lks.com:5601)
- **Logstash**: `http://localstack.lks.com:5044` (TCP Input)

Logstash is configured to receive logs via TCP and index them into Elasticsearch. Kibana can be used to visualize these logs by creating an index pattern (e.g., `logs-*`).


