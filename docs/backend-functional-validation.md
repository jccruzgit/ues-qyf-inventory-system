# Validacion Funcional Backend QYF

Fecha de validacion: 2026-04-07

## Contexto

Se valido el backend Spring Boot del Sistema de Inventario QYF despues del refuerzo de seguridad por laboratorio.

La validacion se dividio en dos niveles:

- HTTP real sobre endpoints expuestos actualmente por la API.
- Pruebas de servicio para la seguridad por laboratorio en flujos que todavia no tienen controlador HTTP.

## Entorno usado

- PostgreSQL Docker existente en `localhost:5433`
- Aplicacion levantada localmente en `http://localhost:8081`
- JWT emitido por `POST /api/auth/login`

Datos funcionales sembrados para la validacion:

- `func_manager` / `FuncPass123!` / `INVENTORY_MANAGER` / `ALL_LABS`
- `func_assigned` / `FuncPass123!` / `VIEWER` / `ASSIGNED_ONLY`
- `func_global` / `FuncPass123!` / `VIEWER` / `ALL_LABS`
- `func_multi` / `FuncPass123!` / `VIEWER` / `MULTI_LAB`
- Laboratorios de prueba: `101` y `102`
- Asignaciones:
  - `func_assigned` -> laboratorio `101`
  - `func_multi` -> laboratorios `101` y `102`

## Endpoints expuestos actualmente

Extraidos desde `GET /v3/api-docs`:

- `POST /api/auth/login`
- `GET /api/health`
- `GET|POST /api/products`
- `GET|PUT /api/products/{id}`
- `PATCH /api/products/{id}/deactivate`
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
- `GET /api/conversions/convert`
- `GET|POST /api/product-documents`
- `GET /api/product-documents/{id}`
- `PATCH /api/product-documents/{id}/deactivate`
- `GET /api/product-documents/product/{productId}`
- `GET|POST /api/users`
- `GET /api/users/{id}`

## Endpoints no expuestos actualmente

No existen controladores HTTP publicados para estos flujos, aunque si existen servicios internos con validacion por laboratorio:

- inventario por laboratorio
- lotes por laboratorio
- movimientos de inventario por laboratorio
- alertas por laboratorio
- laboratorios
- asignaciones `user_laboratories`

Esto significa que hoy no es posible validar esos casos por Postman contra endpoints reales del backend actual. La cobertura ejecutable de esos flujos queda en pruebas de servicio.

## Validacion HTTP ejecutada

### 1. Salud del servicio

- `GET /api/health`
- Resultado: `200 OK`

### 2. Login con JWT

Ejecutado con usuarios `func_manager`, `func_assigned` y `func_global`.

Resultado:

- `200 OK`
- Se recibio `token`
- Se devolvieron `username`, `role`, `fullName` y `accessScope`

Esto confirma funcionalmente que el backend ahora expone `accessScope` en la respuesta de autenticacion.

### 3. Seguridad basica de endpoints autenticados

- `GET /api/products` sin token
- Resultado: `401 Unauthorized`

### 4. Productos

Casos ejecutados:

- `POST /api/products` con `func_manager`
  - Resultado: `201 Created`
- `GET /api/products` con `func_global`
  - Resultado: `200 OK`
- `GET /api/products/1` con `func_assigned`
  - Resultado: `200 OK`
- `GET /api/products/1` con `func_global`
  - Resultado: `200 OK`
- `POST /api/products` con `func_assigned`
  - Resultado: `403 Forbidden`

Conclusiones:

- La autenticacion JWT funciona.
- Las restricciones por rol en productos funcionan.
- Los endpoints de productos no estan ligados a laboratorio en el modelo actual, por lo que no aplican validacion por laboratorio.

## Validacion de seguridad por laboratorio ejecutada

Como no hay endpoints HTTP publicados para los flujos por laboratorio, la validacion ejecutable se hizo mediante pruebas de servicio:

- `LaboratoryAccessServiceImplTest`
- `ProductBatchServiceImplTest`
- `InventoryMovementServiceImplTest`
- `InventoryAlertServiceImplTest`
- `LaboratoryServiceImplTest`

Comando ejecutado:

```bash
mvn "-Dtest=LaboratoryAccessServiceImplTest,ProductBatchServiceImplTest,InventoryMovementServiceImplTest,InventoryAlertServiceImplTest,LaboratoryServiceImplTest" test
```

Resultado:

- `7` pruebas ejecutadas
- `0` fallas
- `0` errores

Cobertura validada:

- usuario autorizado en laboratorio asignado -> permitido
- usuario no autorizado en laboratorio no asignado -> rechazado
- usuario con acceso global -> permitido
- validacion explicita antes de consultar lotes
- validacion explicita antes de consultar movimientos
- validacion explicita antes de consultar alertas
- listado de laboratorios limitado a laboratorios accesibles

## Casos Postman recomendados

Coleccion generada:

- [qyf-inventory-functional-validation.postman_collection.json](/C:/Users/Juan.Rivera/Documents/HorasSociales/ues-qyf-inventory-system/docs/postman/qyf-inventory-functional-validation.postman_collection.json)

Incluye:

- login manager
- login assigned
- login global
- productos sin token
- productos con token
- crear producto con manager
- crear producto con viewer esperando `403`
- ejemplos pendientes para inventario, movimientos, lotes y alertas

## Casos funcionales solicitados y estado real

### Usuario autorizado en laboratorio asignado

Estado:

- Validado en servicios.
- No ejecutable por Postman todavia porque no existe endpoint HTTP publicado para inventario/lotes/movimientos/alertas por laboratorio.

### Usuario no autorizado en laboratorio no asignado

Estado:

- Validado en servicios.
- No ejecutable por Postman todavia por ausencia de endpoints HTTP para esos flujos.

### Usuario con acceso global

Estado:

- Validado en login HTTP.
- Validado en servicios para acceso por laboratorio.

## Mejoras detectadas

- Exponer controladores HTTP para:
  - inventario por laboratorio
  - lotes por laboratorio
  - movimientos por laboratorio
  - alertas por laboratorio
- Exponer endpoints de administracion para:
  - laboratorios
  - asignaciones `user_laboratories`
- Definir un flujo bootstrap para crear el primer usuario administrador sin insertar datos manualmente en DB.
- Documentar oficialmente las rutas que se esperan para inventario, lotes, movimientos y alertas antes de construir colecciones Postman definitivas.
- Corregir texto/encoding en algunos datos semilla que salen con caracteres mal codificados.

