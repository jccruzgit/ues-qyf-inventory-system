# Recipe-Based Discharge Validation

Fecha de validacion: `2026-05-25`
Rama validada: `feature/recipe-based-discharge`

## Alcance

Se valido la feature de descargo por receta sin agregar nuevas funcionalidades. La validacion cubrio:

- backend tests;
- frontend build;
- migraciones Flyway desde base limpia;
- flujo exitoso de producto elaborado + receta + elaboracion + confirmacion;
- validacion de bloqueo por stock insuficiente sin descargas parciales;
- reversion del movimiento y restauracion de stock;
- descargo individual previo;
- endpoints consumidos por pantallas de `Insumos`, `Inventario`, `Lotes` y `Movimientos`.

## Comandos ejecutados

### Backend

Nota: el repo no incluye `mvnw`; la validacion equivalente se ejecuto con `mvn`.

```powershell
mvn clean test
mvn '-Dmaven.test.skip=true' package
java -jar backend\inventory\target\inventory-backend-0.0.1-SNAPSHOT.jar `
  --server.port=8080 `
  --spring.datasource.url=jdbc:postgresql://localhost:5433/qyf_inventory_validation `
  --spring.datasource.username=qyf_user `
  --spring.datasource.password=qyf_password `
  --app.demo.seed.enabled=true `
  --app.default-admin.enabled=true
```

### Frontend

```powershell
npm.cmd run build
```

### Base de datos / Flyway

```powershell
& 'C:\Program Files\PostgreSQL\17\bin\psql.exe' -h localhost -p 5433 -U qyf_user -d qyf_inventory `
  -c "DROP DATABASE IF EXISTS qyf_inventory_validation;"
& 'C:\Program Files\PostgreSQL\17\bin\psql.exe' -h localhost -p 5433 -U qyf_user -d qyf_inventory `
  -c "CREATE DATABASE qyf_inventory_validation;"
& 'C:\Program Files\PostgreSQL\17\bin\psql.exe' -h localhost -p 5433 -U qyf_user -d qyf_inventory_validation `
  -c "SELECT count(*), string_agg(version, ',' ORDER BY installed_rank) FROM flyway_schema_history WHERE success;"
```

### Validacion funcional por API

Se ejecutaron scripts PowerShell con `Invoke-RestMethod` contra:

- `POST /api/auth/login`
- `GET /api/laboratories`
- `GET /api/products`
- `GET /api/product-batches`
- `GET /api/inventory-stock`
- `POST /api/manufactured-products`
- `POST /api/recipes`
- `POST /api/recipes/{id}/items`
- `POST /api/production-runs`
- `POST /api/production-runs/{id}/confirm`
- `GET /api/inventory-movements/{id}`
- `POST /api/inventory-movements/{id}/reverse`
- `POST /api/inventory-movements`

## Resultado de pruebas

### 1. Backend tests

- `mvn clean test`: `OK`
- Resultado: `49` tests, `0` failures, `0` errors, `0` skipped.

### 2. Frontend build

- `npm.cmd run build`: `OK`
- Resultado: build Vite exitosa.
- Observacion: Vite reporta warning de chunk grande para `assets/index-DTWW-qWK.js` (`634.28 kB` minificado), pero no bloquea el build.

### 3. Flyway desde base limpia

- Base usada: `qyf_inventory_validation`
- Resultado: `OK`
- Evidencia: `12` migraciones aplicadas, versiones `1` a `12`.

### 4. Crear producto elaborado

- Resultado: `OK`
- Creado:
  - `manufacturedProductId = 2`
  - `code = ELAB-VAL-20260525202453`

### 5. Crear receta con varios insumos

- Resultado: `OK`
- Receta creada:
  - `successRecipeId = 2`
  - `code = REC-VAL-20260525202453`
- Insumos agregados:
  - `AGUA-DEST-001`: `100 ml`
  - `GLIC-001`: `50 ml`
  - `ESEN-CIT-001`: `10 ml`

### 6. Crear elaboracion

- Resultado: `OK`
- Elaboracion creada:
  - `successProductionRunId = 1`
  - estado inicial: `DRAFT`
  - `readyToConfirm = true`

### 7. Confirmar elaboracion con stock suficiente

- Resultado: `OK`
- Estado posterior: `CONFIRMED`
- Movimiento generado: `inventoryMovementId = 3`

### 8. Confirmar que se genera movimiento EXIT con multiples lineas

- Resultado: `OK`
- Movimiento `3`:
  - `movementType = EXIT`
  - `productionRunId = 1`
  - `recipeId = 2`
  - `manufacturedProductId = 2`
  - `movementLineCount = 3`
  - lineas para:
    - `AGUA-DEST-001`
    - `GLIC-001`
    - `ESEN-CIT-001`

### 9. Confirmar que el stock disminuye

- Resultado: `OK`
- Antes de confirmar:
  - agua: `2000`
  - glicerina: `600`
  - esencia: `200`
- Despues de confirmar:
  - agua: `1900`
  - glicerina: `550`
  - esencia: `190`

### 10. Confirmar que no permite confirmar si un insumo no tiene stock suficiente

- Resultado: `OK`
- Escenario creado:
  - `failureRecipeId = 3`
  - agua requerida: `5000`
  - agua disponible al momento del intento: `1900`
- `readyToConfirm = false`
- `POST /api/production-runs/2/confirm` respondio `400`.

### 11. Confirmar que no genera descargas parciales si falla un insumo

- Resultado: `OK`
- Conteo de movimientos antes del intento fallido: `3`
- Conteo de movimientos despues del intento fallido: `3`
- Stock antes:
  - agua: `1900`
  - glicerina: `550`
- Stock despues:
  - agua: `1900`
  - glicerina: `550`

### 12. Confirmar que la reversion del movimiento restaura stock

- Resultado: `OK`
- Reversion creada: `movementId = 4`
- Stock despues de revertir:
  - agua: `2000`
  - glicerina: `600`
  - esencia: `200`

### 13. Confirmar que el descargo individual anterior sigue funcionando

- Resultado: `OK`
- Movimiento individual creado: `movementId = 5`
- Producto probado: `ALCO-001`
- Stock antes: `20`
- Stock despues: `19`

### 14. Confirmar que las pantallas de Insumos, Inventario, Lotes y Movimientos no se rompieron

- Validacion realizada:
  - `GET /api/products`: `200`
  - `GET /api/inventory-stock?laboratoryId=1`: `200`
  - `GET /api/product-batches?productId=1&laboratoryId=1`: `200`
  - `GET /api/inventory-movements`: `200`
  - `npm.cmd run build`: `OK`
- Resultado: `OK` a nivel de contrato API + build.
- Limite: no se ejecuto validacion visual con navegador automatizado en este pase.

## Bugs corregidos

- Ninguno.
- No fue necesario modificar codigo de backend ni frontend para aprobar la validacion.

## Pendientes detectados

- El repositorio no incluye `mvnw`; si el equipo quiere exigir `mvnw clean test` de forma literal en CI o validaciones manuales, hace falta agregar el wrapper de Maven.
- La validacion de las pantallas `Insumos`, `Inventario`, `Lotes` y `Movimientos` fue por build y contratos API; queda pendiente una corrida UI automatizada o manual con navegador para validar rendering visual y navegacion completa.
- El build del frontend deja un warning de chunk grande en Vite (`assets/index-DTWW-qWK.js`); no rompe la feature, pero conviene seguirlo aparte si quieren reducir peso del bundle.

## Conclusión

La feature de descargo por receta quedo validada funcionalmente:

- pasa backend tests;
- pasa frontend build;
- Flyway aplica desde base limpia;
- genera `EXIT` con multiples lineas;
- descuenta stock correctamente;
- bloquea confirmacion con stock insuficiente;
- evita descargas parciales;
- revierte restaurando stock;
- mantiene operativo el descargo individual previo.
