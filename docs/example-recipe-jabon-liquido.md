# Ejemplo completo: receta, insumos, elaboracion y descargo

## Objetivo

Este documento describe un caso de prueba completo usando un producto elaborado de ejemplo: `Jabon liquido citrico`.

El flujo cubre:

1. ingreso de insumos con lote y cantidad;
2. existencia del producto elaborado;
3. existencia de la receta;
4. elaboracion desde la receta;
5. descargo por elaboracion;
6. verificacion en stock y movimientos;
7. reversion del movimiento.

## Datos del ejemplo

### Laboratorio

- Codigo: `LAB-DEMO`
- Nombre: `Laboratorio Demo QYF`

### Producto elaborado

- Codigo: `ELAB-JAB-001`
- Nombre: `Jabon liquido citrico`

### Receta

- Codigo: `REC-JAB-001`
- Nombre: `Formula base de jabon liquido citrico`

### Insumos requeridos por receta

| Insumo | Codigo | Cantidad por elaboracion | Unidad |
|---|---|---:|---|
| Agua destilada | `AGUA-DEST-001` | 500 | ml |
| Glicerina liquida | `GLIC-001` | 120 | ml |
| Esencia citrica | `ESEN-CIT-001` | 20 | ml |

## Seed opcional para pruebas

Si el backend se levanta con:

- `APP_DEMO_SEED_ENABLED=true`

el sistema intenta sembrar automaticamente:

- el laboratorio `LAB-DEMO`;
- los insumos del ejemplo;
- el producto elaborado `ELAB-JAB-001`;
- la receta `REC-JAB-001`;
- lotes iniciales para los insumos.

### Lotes sembrados para la receta de ejemplo

| Insumo | Lote | Cantidad inicial | Vencimiento aproximado |
|---|---|---:|---|
| Agua destilada | `DEMO-AGUA-001` | 2000 ml | hoy + 180 dias |
| Glicerina liquida | `DEMO-GLIC-001` | 600 ml | hoy + 150 dias |
| Esencia citrica | `DEMO-ESEN-001` | 200 ml | hoy + 90 dias |

Tambien pueden existir otros insumos demo ya sembrados por el sistema, como `ALCO-001` y `ACET-001`.

## Flujo de prueba manual

### 1. Verificar o registrar insumos

Si usas el seed demo, primero valida en la UI:

1. Ir a `Insumos`.
2. Confirmar que existen:
   - `AGUA-DEST-001`
   - `GLIC-001`
   - `ESEN-CIT-001`
3. Ir a `Lotes` o `Inventario`.
4. Confirmar que existen los lotes:
   - `DEMO-AGUA-001`
   - `DEMO-GLIC-001`
   - `DEMO-ESEN-001`

Si no usas el seed, registra manualmente esos insumos y luego crea una entrada por lote para cada uno usando los mismos datos base del ejemplo.

### 2. Verificar o registrar producto elaborado

1. Ir a `Productos elaborados`.
2. Confirmar que existe `ELAB-JAB-001 - Jabon liquido citrico`.
3. Si no existe, crearlo manualmente con ese codigo y nombre.

## 3. Verificar o registrar receta

1. Ir a `Recetas`.
2. Confirmar que existe `REC-JAB-001 - Formula base de jabon liquido citrico`.
3. Verificar que la composicion tenga estas lineas:
   - Agua destilada: 500 ml
   - Glicerina liquida: 120 ml
   - Esencia citrica: 20 ml

Si no existe, crearla manualmente con esos mismos datos.

## 4. Ejecutar una elaboracion

1. Ir a `Elaboracion`.
2. Seleccionar:
   - Producto elaborado: `Jabon liquido citrico`
   - Receta: `REC-JAB-001`
   - Laboratorio: `LAB-DEMO`
3. Preparar la elaboracion.
4. Verificar en la previsualizacion:
   - insumos requeridos;
   - cantidades correctas;
   - lote sugerido para cada insumo;
   - ubicacion disponible;
   - advertencia previa a confirmar.

### Resultado esperado en la previsualizacion

Como el ejemplo tiene un lote por insumo, el sistema deberia sugerir:

- `DEMO-AGUA-001` para Agua destilada
- `DEMO-GLIC-001` para Glicerina liquida
- `DEMO-ESEN-001` para Esencia citrica

## 5. Confirmar la elaboracion

1. Confirmar la elaboracion.
2. El sistema debe generar un movimiento `EXIT`.
3. Ese movimiento debe quedar asociado a la elaboracion y a la receta.

## 6. Verificar stock despues del descargo

Despues de confirmar una elaboracion, el stock esperado del ejemplo queda asi:

| Insumo | Stock inicial | Consumo por receta | Stock esperado |
|---|---:|---:|---:|
| Agua destilada | 2000 ml | 500 ml | 1500 ml |
| Glicerina liquida | 600 ml | 120 ml | 480 ml |
| Esencia citrica | 200 ml | 20 ml | 180 ml |

Validacion sugerida:

1. Ir a `Inventario`.
2. Buscar cada insumo.
3. Confirmar que el stock consolidado disminuyo.
4. Ir a `Lotes`.
5. Confirmar que el mismo lote quedo con menos cantidad disponible.

## 7. Verificar historial de movimientos

1. Ir a `Movimientos`.
2. Filtrar por laboratorio `LAB-DEMO`.
3. Ubicar el movimiento `EXIT` recien generado.
4. Confirmar que:
   - tenga varias lineas;
   - cada linea corresponda a un insumo de la receta;
   - se vea la referencia a elaboracion o receta en la tabla;
   - la fecha, usuario y observaciones queden registradas.

## 8. Verificar reversion

1. Desde `Movimientos`, reversar el movimiento generado por la elaboracion.
2. Confirmar la reversion con un motivo.
3. Volver a `Inventario` y `Lotes`.

### Resultado esperado despues de la reversion

Los valores deben regresar al estado anterior:

| Insumo | Stock restaurado esperado |
|---|---:|
| Agua destilada | 2000 ml |
| Glicerina liquida | 600 ml |
| Esencia citrica | 200 ml |

Tambien debe aparecer un nuevo movimiento compensatorio en historial.

## 9. Alcance funcional del ejemplo

Este ejemplo valida:

- gestion de producto elaborado;
- gestion de receta;
- detalle de receta con insumos;
- elaboracion basada en receta;
- descargo multi-linea con trazabilidad;
- validacion de stock suficiente;
- verificacion de movimientos;
- reversion del descargo.

## 10. Limitacion importante

En esta primera version, la elaboracion descuenta insumos del inventario, pero no registra entrada de stock para el producto elaborado final. El producto elaborado funciona como referencia operativa y academica para la receta y la trazabilidad del descargo.
