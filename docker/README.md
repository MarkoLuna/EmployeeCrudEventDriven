# Infrastructure & Service Dependencies

This directory contains the Docker Compose configurations and supporting files required to run the infrastructure dependencies for the **Employee CRUD Event-Driven System**.

## Services Overview

The [keycloak-compose.yml](keycloak-compose.yml) file manages the following services:

| Service | Technology | Port | Purpose |
| :--- | :--- | :--- | :--- |
| **IAM** | Keycloak | `8081` | Identity and Access Management (OAuth2/OIDC) |
| **Identity DB** | PostgreSQL | `5432` | Persistence for Keycloak users and realms |
| **Data Store** | MongoDB | `27017` | Persistent storage for the Employee Consumer service |
| **Message Broker**| Kafka | `9092` | Asynchronous event delivery |
| **Coordinator** | Zookeeper | `2181` | Distributed coordination for Kafka |
| **Reverse Proxy** | Nginx | `8080` | Entry point for services and authentication |

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

### Start Infrastructure
Run the following command to start all dependencies in the background:
```bash
docker compose up -d
```

### Stop Infrastructure
To stop and remove all containers:
```bash
docker compose down
```

### View Logs
```bash
docker compose logs -f [service_name]
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

*Note: Ensure the backend services are running on your host machine to be reachable by the proxy. Access them via `http://localstack.lks.com:8080/[path]`.*