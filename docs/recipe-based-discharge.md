# Descargo por receta

## 1. Explicacion del nuevo flujo

Esta primera version agrega una capa de elaboracion sobre el inventario existente. El flujo esperado es:

1. El encargado crea un producto elaborado.
2. El encargado crea una receta para ese producto elaborado.
3. La receta agrega uno o mas insumos del catalogo actual con sus cantidades y unidad de medida.
4. El usuario entra al modulo de `Elaboracion`.
5. Selecciona producto elaborado, receta y laboratorio.
6. El sistema prepara una previsualizacion con insumos requeridos, cantidades, lote sugerido y ubicacion disponible.
7. Antes de confirmar, el sistema advierte que se descargaran todos los insumos de la receta.
8. Al confirmar, el backend genera un movimiento `EXIT` con multiples lineas.
9. El movimiento queda relacionado con la elaboracion para mantener trazabilidad.

## 2. Diferencia entre insumo y producto elaborado

- `Insumo`: corresponde al modelo actual `Product` del backend. Sigue siendo el elemento inventariable que entra por lote, mantiene stock y se descarga del inventario.
- `Producto elaborado`: representa lo que el laboratorio fabrica a partir de una receta, por ejemplo jabon liquido, jarabe o crema base.

En esta fase solo se cambio el lenguaje visible del frontend para que el usuario vea "Insumo" en los flujos actuales de inventario. No se hizo un renombrado masivo de entidades o tablas existentes.

## 3. Diferencia entre descargo individual y descargo por receta

- `Descargo individual`: se mantiene para perdidas, desperdicio, prestamos u otros casos especiales. Sigue funcionando con seleccion manual de insumo, laboratorio y lote.
- `Descargo por receta`: prepara y confirma una elaboracion. Usa la receta para descargar varios insumos en una sola operacion trazable.

## 4. Entidades nuevas creadas

Se agregaron nuevas entidades y tablas para soportar la elaboracion:

- `manufactured_products`
- `recipes`
- `recipe_items`
- `production_runs`

Resumen funcional:

- `manufactured_products`: catalogo de productos que el laboratorio fabrica.
- `recipes`: cabecera de la receta asociada a un producto elaborado.
- `recipe_items`: detalle de insumos, cantidades, unidad de medida y observaciones de la receta.
- `production_runs`: ejecucion de una elaboracion por usuario, laboratorio y receta, con enlace al movimiento generado.

## 5. Endpoints agregados

Se expusieron endpoints REST para la primera version:

- `GET /api/manufactured-products`
- `POST /api/manufactured-products`
- `PUT /api/manufactured-products/{id}`
- `GET /api/recipes`
- `GET /api/recipes/{id}`
- `POST /api/recipes`
- `PUT /api/recipes/{id}`
- `POST /api/recipes/{id}/items`
- `DELETE /api/recipes/{id}/items/{itemId}`
- `GET /api/production-runs/{id}`
- `POST /api/production-runs`
- `POST /api/production-runs/{id}/confirm`

## 6. Reglas de negocio

- Una receta debe tener al menos un insumo.
- Todas las cantidades deben ser mayores a cero.
- Un producto elaborado puede tener varias recetas, pero para esta fase se recomienda manejar una receta activa.
- Una elaboracion confirmada genera un movimiento de salida.
- No se permiten descargas parciales si un insumo no tiene stock suficiente.
- No se permite stock negativo.
- Se conserva trazabilidad por usuario, fecha, laboratorio, receta, elaboracion y movimiento.
- El movimiento original no se edita ni se elimina.
- Las reversiones siguen funcionando sobre movimientos generados por elaboracion.

## 7. Flujo de prueba manual

Flujo sugerido para validar la funcionalidad de extremo a extremo:

1. Crear un producto elaborado desde `Productos elaborados`.
2. Crear una receta desde `Recetas`.
3. Asociar el producto elaborado.
4. Agregar al menos un insumo con cantidad mayor a cero.
5. Registrar entradas por lote para los insumos requeridos desde `Nueva entrada`.
6. Ir a `Elaboracion`.
7. Seleccionar producto elaborado, receta y laboratorio.
8. Preparar la elaboracion y verificar:
   - insumos requeridos;
   - cantidades;
   - lote sugerido;
   - ubicacion mostrada;
   - advertencia antes de confirmar.
9. Confirmar la elaboracion.
10. Verificar que se genere el movimiento de salida en `Movimientos`.
11. Verificar que el stock disminuya en `Inventario`.
12. Reversar el movimiento desde `Movimientos`.
13. Verificar que el stock vuelva al valor anterior.

## 8. Limitaciones de esta primera version

- El catalogo inventariable sigue usando la entidad `Product` en backend; el cambio a "Insumo" es solo visual en frontend.
- No se implemento escalado de recetas.
- No se implementaron costos automaticos por producto elaborado.
- No se implemento QR.
- No se implemento importacion por Excel.
- La seleccion de lote usa sugerencia FEFO basada en la disponibilidad actual y fecha de vencimiento registrada.
- No se personalizo aun una vista dedicada de historial solo para elaboraciones; la trazabilidad se consulta dentro del historial general de movimientos.
- La validacion manual completa depende de contar con el backend levantado, datos base y acceso funcional a la interfaz.

## 9. Pendientes futuros

- Escalado de receta.
- Costos por producto elaborado.
- QR.
- Reportes.
- Importacion por Excel.
