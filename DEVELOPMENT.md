# Development Guide

This document provides guidelines for developers working on the E-Commerce API project.

## Development Environment Setup

### Prerequisites

- Java 21 or newer
- Maven 3.8+
- Docker and Docker Compose
- Git
- Docker Desktop running (required for Testcontainers)

### Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/your-repo/ecom-api.git
   cd ecom-api
   ```

2. Make the scripts executable:
   ```bash
   chmod +x *.sh
   ```

3. Start the development environment:
   ```bash
   ./dev-start.sh
   ```

   This will:
   - Build the Spring Boot application
   - Start all the required containers (PostgreSQL, Redis, Keycloak, etc.)
   - Configure necessary data volumes

4. Access the services:
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Keycloak: http://localhost:8081 (admin/admin)
   - Adminer (DB): http://localhost:8082 (Server: db, User: ecommerce_user, Password: ecommerce_pass)
   - Prometheus: http://localhost:9090
   - Grafana: http://localhost:3000 (admin/admin)

### Development Workflow

1. Make changes to the code
2. Rebuild and restart the API container:
   ```bash
   ./dev-rebuild.sh
   ```

3. View logs:
   ```bash
   docker-compose logs -f app
   ```

4. Stop all services when done:
   ```bash
   docker-compose down
   ```

### Database Migrations

We use Flyway for database migrations. Migration scripts should be placed in:
```
src/main/resources/db/migration
```

Follow the naming convention:
```
V{version}__{description}.sql
```

Example: `V1__create_users_table.sql`

### Testing

Run unit tests:
```bash
./mvnw test
```

Run integration tests:
```bash
./mvnw verify
```

### Useful Commands

- Access PostgreSQL CLI:
  ```bash
  docker-compose exec db psql -U ecommerce_user -d ecommerce
  ```

- Access Redis CLI:
  ```bash
  docker-compose exec redis redis-cli
  ```

- View API logs:
  ```bash
  docker-compose logs -f app
  ```

- Rebuild a specific service:
  ```bash
  docker-compose up -d --build --no-deps <service-name>
  ```
