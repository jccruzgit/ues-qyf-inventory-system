# Modulo de Lotes

## Descripcion

La seccion **Lotes** expone el detalle real de los lotes registrados en inventario y reemplaza el placeholder anterior del frontend. La pantalla consulta el backend mediante `GET /api/product-batches/overview` y presenta informacion consolidada por lote a partir de:

- `product_batches`
- movimientos de inventario
- stock calculado por lote
- metadatos del producto y laboratorio

La vista no modifica el flujo transaccional existente. Solo consume una lectura enriquecida para mostrar el estado actual de cada lote.

## Datos mostrados

Cada tarjeta de lote incluye:

- producto
- codigo del producto
- codigo de lote
- laboratorio
- ubicacion del producto, si existe
- cantidad disponible
- unidad de medida
- fecha de vencimiento
- precio unitario mas reciente de entrada, si existe
- estado operativo del lote
- observaciones del lote

## Filtros disponibles

La pantalla incorpora filtros basicos para consulta operativa:

- busqueda libre por nombre de producto, codigo de producto o codigo de lote
- filtro por laboratorio
- filtro por estado operativo:
  - `Vigente`
  - `Proximo a vencer`
  - `Vencido`
  - `Agotado`

## Reglas de calculo de stock por lote

El endpoint `GET /api/product-batches/overview` calcula el saldo por lote recorriendo movimientos accesibles al usuario y acumulando por `batch_id`.

Reglas aplicadas:

1. cada `ENTRY` suma cantidad al lote;
2. cada `EXIT` resta cantidad al lote;
3. las reversiones se reflejan automaticamente porque tambien son movimientos;
4. un lote con saldo `0` se mantiene visible en la seccion y se presenta como `Agotado`;
5. el precio unitario mostrado corresponde al ultimo movimiento de tipo `ENTRY` con precio informado para ese lote;
6. el estado operativo visible en frontend se determina asi:
   - `Agotado` si el saldo es `0` o menor;
   - `Vencido` si la fecha ya paso o si el lote viene marcado como `EXPIRED`;
   - `Proximo a vencer` si faltan `30` dias o menos;
   - `Vigente` en los demas casos.

Nota: si el lote esta en `QUARANTINED`, la vista mantiene el badge visible como `Cuarentena`, pero el filtro operativo sigue usando los cuatro estados anteriores.

## Flujo de prueba manual

1. abrir la seccion **Lotes** y confirmar que carga datos reales del backend;
2. registrar una nueva entrada con producto controlado por lote;
3. volver a **Lotes** y verificar que el lote aparece con cantidad, fecha, precio unitario y observaciones;
4. registrar una salida parcial del mismo lote;
5. volver a **Lotes** y verificar que la cantidad disponible disminuye;
6. reversar la salida desde **Movimientos**;
7. volver a **Lotes** y verificar que la cantidad disponible aumenta nuevamente;
8. reversar la entrada original desde **Movimientos**;
9. volver a **Lotes** y verificar que el lote queda en `Agotado` o sin stock disponible segun el saldo resultante.
