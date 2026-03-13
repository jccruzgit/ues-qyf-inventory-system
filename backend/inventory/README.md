# Inventory Backend

Base backend project for the UES QYF Inventory System.

## Requirements

- Java 17
- Maven 3.9+
- PostgreSQL 14+

## Configuration

The application uses these environment variables:

- `DB_URL` (default: `jdbc:postgresql://localhost:5432/qyf_inventory`)
- `DB_USERNAME` (default: `postgres`)
- `DB_PASSWORD` (default: `postgres`)
- `SERVER_PORT` (default: `8080`)

## Run locally

1. Create a PostgreSQL database named `qyf_inventory`.
2. Export the database environment variables if your local values are different.
3. Start the application:

```bash
mvn spring-boot:run
```

## Build

```bash
mvn clean package
```

## Useful endpoints

- Health check: `GET /api/health`
- OpenAPI JSON: `GET /v3/api-docs`
- Swagger UI: `GET /swagger-ui.html`
