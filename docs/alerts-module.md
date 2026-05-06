# Modulo de Alertas

## Tipos de alertas implementadas

La seccion **Alertas** consume datos reales desde el backend y presenta estas alertas:

- `LOW_STOCK`: producto con stock actual mayor que cero y menor o igual al stock minimo configurado.
- `OUT_OF_STOCK`: producto sin existencias disponibles.
- `EXPIRING_BATCH`: lote con existencias disponibles y fecha de vencimiento dentro de los proximos 30 dias.
- `EXPIRED_BATCH`: lote con existencias disponibles y fecha de vencimiento vencida o igual al dia actual.

## Reglas usadas para generarlas

Las reglas se calculan en backend y no se duplican en frontend.

Reglas activas:

1. la sincronizacion se ejecuta al registrar entradas, salidas y reversiones mediante `InventoryMovementServiceImpl`;
2. `LOW_STOCK` se crea cuando `current_stock <= minimum_stock` y `current_stock > 0`;
3. `OUT_OF_STOCK` se crea cuando `current_stock <= 0`;
4. las alertas de lote se recalculan solo para lotes activos con fecha de vencimiento;
5. si un lote ya no tiene existencias disponibles, no mantiene alertas de vencimiento pendientes;
6. la prioridad visible se deriva del tipo:
   - `CRITICA` para `OUT_OF_STOCK` y `EXPIRED_BATCH`;
   - `ALTA` para `LOW_STOCK` y vencimientos muy cercanos;
   - `MEDIA` para el resto de lotes proximos a vencer.

## Endpoints utilizados

Pantalla de Alertas:

- `GET /api/inventory-alerts`
  - parametros soportados:
    - `laboratoryId` opcional;
    - `alertType` opcional;
    - `pendingOnly` opcional.

Dashboard:

- `GET /api/dashboard/summary`
- `GET /api/inventory-alerts?pendingOnly=true`

## Flujo de prueba manual

1. crear o identificar un producto con stock minimo configurado;
2. verificar caso `stock bajo` con una salida que deje `stock actual < stock minimo`;
3. verificar caso `stock igual al minimo` con una salida que deje `stock actual == stock minimo`;
4. verificar caso `sin stock` con una salida que lleve el producto a `0`;
5. registrar una entrada que corrija el nivel y confirmar que desaparece la alerta de stock correspondiente;
6. registrar o ubicar un lote con vencimiento dentro de 30 dias y confirmar alerta `proximo a vencer`;
7. registrar o ubicar un lote con fecha vencida y confirmar alerta `vencido`;
8. confirmar que un producto sin condiciones de riesgo no aparece en la seccion;
9. reversar una salida o una entrada y verificar que la lista de alertas cambia tras recargar la vista.

## Limitaciones conocidas

- en esta fase no se implementa marcado manual de alerta como resuelta o atendida;
- la seccion de Alertas muestra estado derivado desde backend, pero sin flujo de acknowledgement desde UI;
- `sin stock` se implementa a nivel producto, no como alerta independiente por lote agotado;
- la actualizacion visual depende de recarga o nueva consulta del frontend despues de operaciones.

## Pendientes futuros

- endpoint para marcar alertas como atendidas o resueltas;
- notificaciones por correo, push o canales externos;
- programacion automatica de sincronizacion fuera del flujo transaccional;
- reglas mas finas por severidad, ventanas configurables de vencimiento y alertas por lote agotado.
