# Inventory Backend

Spring Boot backend for the "Sistema de Inventario para la Facultad de Quimica y Farmacia - UES".

## Stack

- Java 17
- Spring Boot 3.3
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Flyway
- Springdoc OpenAPI / Swagger UI
- JUnit 5 + Testcontainers

## Response contract

Except for transport-level failures, the API returns a shared wrapper:

```json
{
  "success": true,
  "message": "Products retrieved successfully",
  "data": [],
  "timestamp": "2026-04-08T22:00:00"
}
```

Error responses keep the same shape with `success=false`.

## Main endpoints

### Public

- `POST /api/auth/login`
  - Purpose: authenticate user and return JWT
  - Request:
    ```json
    {
      "username": "admin",
      "password": "Admin123*"
    }
    ```
  - Response codes: `200`, `400`, `401`

- `GET /api/health`
  - Purpose: health check
  - Response codes: `200`

- `GET /v3/api-docs`
  - Purpose: OpenAPI JSON

- `GET /swagger-ui.html`
  - Purpose: Swagger UI

### Protected catalog endpoints

- `GET|POST /api/categories`
- `GET|PUT /api/categories/{id}`
- `PATCH /api/categories/{id}/deactivate`

- `GET|POST /api/locations`
- `GET|PUT /api/locations/{id}`
- `PATCH /api/locations/{id}/deactivate`

- `GET|POST /api/units`
- `GET|PUT /api/units/{id}`
- `PATCH /api/units/{id}/deactivate`

- `GET|POST /api/conversions`
- `GET|PUT /api/conversions/{id}`
- `PATCH /api/conversions/{id}/deactivate`
- `GET /api/conversions/convert?sourceUnitId={id}&targetUnitId={id}&quantity={value}`

- `GET|POST /api/products`
- `GET|PUT /api/products/{id}`
- `PATCH /api/products/{id}/deactivate`

- `GET|POST /api/product-documents`
- `GET /api/product-documents/product/{productId}`
- `GET /api/product-documents/{id}`
- `PATCH /api/product-documents/{id}/deactivate`

- `GET /api/laboratories`
- `GET /api/laboratories/{id}`

- `GET|POST /api/users`
- `GET /api/users/{id}`

### Protected inventory endpoints

- `POST /api/inventory-movements`
  - Purpose: register `ENTRY` or `EXIT`
  - Request:
    ```json
    {
      "movementType": "ENTRY",
      "laboratoryId": 1,
      "observation": "Initial inventory",
      "lines": [
        {
          "productId": 10,
          "productBatchId": null,
          "batchCode": "LOT-001",
          "expirationDate": "2026-12-31",
          "quantity": 100,
          "lineNotes": "Demo batch"
        }
      ]
    }
    ```
  - Response codes: `201`, `400`, `401`, `403`, `404`

- `GET /api/inventory-movements`
  - Purpose: movement history
  - Query params:
    - `productId`
    - `laboratoryId`
    - `movementType`
    - `dateFrom`
    - `dateTo`
  - Response codes: `200`, `400`, `401`, `403`

- `GET /api/inventory-movements/{id}`
  - Purpose: movement detail
  - Response codes: `200`, `401`, `403`, `404`

- `GET /api/inventory-stock`
  - Purpose: stock query by product, laboratory or batch
  - Query params:
    - `productId`
    - `laboratoryId`
    - `productBatchId`
  - Response codes: `200`, `400`, `401`, `403`, `404`

- `GET /api/product-batches?productId={id}&laboratoryId={id}`
  - Purpose: list batches for a product inside a laboratory
  - Response codes: `200`, `401`, `403`, `404`

- `GET /api/product-batches/{id}`
  - Purpose: batch detail
  - Response codes: `200`, `401`, `403`, `404`

- `GET /api/inventory-alerts?laboratoryId={id}&alertType={type}&pendingOnly={true|false}`
  - Purpose: low stock, expiring batch and expired batch alerts
  - Response codes: `200`, `400`, `401`, `403`, `404`

## Product payloads

### Create product

`POST /api/products`

```json
{
  "code": "PRD-001",
  "name": "Acetona",
  "description": "Reactivo",
  "categoryId": 1,
  "baseUnitId": 1,
  "minimumStock": 10,
  "currentStock": 0,
  "locationId": 1,
  "observations": "Catalog item",
  "storageCondition": "Dry place",
  "requiresExpiration": true,
  "requiresBatchControl": true,
  "active": true
}
```

### Update product

`PUT /api/products/{id}`

`currentStock` is not accepted as an updatable field. If it is sent, it is ignored by the backend.

```json
{
  "code": "PRD-001",
  "name": "Acetona grado analitico",
  "description": "Updated metadata",
  "categoryId": 1,
  "baseUnitId": 1,
  "minimumStock": 15,
  "locationId": 1,
  "observations": "Only metadata changes",
  "storageCondition": "Dry place",
  "requiresExpiration": true,
  "requiresBatchControl": true,
  "active": true
}
```

## Common response codes

- `200 OK`: successful read or update
- `201 Created`: successful creation
- `400 Bad Request`: validation error, malformed payload, invalid enum/date, insufficient stock
- `401 Unauthorized`: missing or invalid JWT
- `403 Forbidden`: authenticated but without required role or lab access
- `404 Not Found`: entity does not exist or is inactive
- `409 Conflict`: duplicate resource
- `500 Internal Server Error`: unexpected server-side failure

## Local setup

### 1. Start PostgreSQL

Example with Docker:

```bash
docker run -d --name qyf-postgres ^
  -e POSTGRES_DB=qyf_inventory ^
  -e POSTGRES_USER=postgres ^
  -e POSTGRES_PASSWORD=postgres ^
  -p 5433:5432 ^
  postgres:16
```

### 2. Configure environment

Defaults from `application.yml`:

- `DB_URL=jdbc:postgresql://localhost:5433/qyf_inventory`
- `DB_USERNAME=postgres`
- `DB_PASSWORD=postgres`
- `SERVER_PORT=8080`
- `JWT_SECRET=ues-qyf-inventory-system-jwt-secret-key-2026`
- `JWT_EXPIRATION_MS=86400000`

Optional demo seed:

- `APP_DEMO_SEED_ENABLED=true`

### 3. Run the backend

```bash
cd backend/inventory
mvn spring-boot:run
```

## Demo seed

If `APP_DEMO_SEED_ENABLED=true`, startup seeds:

- admin user
- demo laboratory
- catalog base data
- demo products
- initial inventory movement

Default local demo credentials:

- Username: `admin`
- Password: `Admin123*`

Seeded demo state:

- laboratory `LAB-DEMO`
- product `ALCO-001` with low stock and near expiration
- product `ACET-001` with healthy stock

## Tests

Run the full backend suite:

```bash
cd backend/inventory
mvn test
```

Run only the inventory-focused suite:

```bash
cd backend/inventory
mvn "-Dtest=InventoryMovementServiceImplTest,InventoryStockServiceImplTest,InventoryAlertServiceImplTest,PostgreSqlPersistenceIntegrationTest" test
```

## Swagger

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Swagger now exposes the JWT bearer scheme used by the protected endpoints.
