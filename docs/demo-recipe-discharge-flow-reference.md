# Referencia de demo: flujo completo de formula, elaboracion, descargo e impresion

## Objetivo

Este documento resume el flujo funcional que ya puede mostrarse en una nueva demo del modulo de `Producto a elaborar`, `Formula` y `Elaboracion`.

Sirve como referencia rapida para:

1. preparar el entorno de demo;
2. recorrer el flujo principal en orden;
3. usar un ejemplo concreto de prueba;
4. validar resultados esperados;
5. mostrar casos de control como variacion y trazabilidad.

## Alcance del flujo actual

El flujo implementado cubre:

1. creacion o edicion de `Producto a elaborar` con `grupo`, `ciclo` y `lote`;
2. creacion de `Formula` teorica asociada al producto;
3. preparacion de una `Elaboracion` basada en la formula;
4. captura de `cantidades reales` por insumo;
5. validacion de variacion maxima del `10%`;
6. confirmacion del descargo real;
7. registro trazable del movimiento de salida;
8. impresion de `Formula` teorica;
9. impresion de `Descargo` real;
10. reversion del movimiento desde historial.

## Lo que aun no forma parte de la demo

Todavia no se considera cerrado en este flujo:

1. modelo academico final de `alumno` y `profesor`;
2. autorizacion formal del profesor para cambios excepcionales;
3. restriccion fuerte por grupo en autenticacion;
4. reglas UI/API finales atadas a ese modelo academico.

## Datos base recomendados para la demo

### Credenciales demo

- Usuario: `admin`
- Password: `Admin123*`

### Laboratorio recomendado

- Codigo: `LAB-DEMO`
- Nombre esperado: `Laboratorio Demo QYF`

### Producto a elaborar de ejemplo

- Codigo: `ELAB-JAB-001`
- Nombre: `Jabon liquido citrico`
- Grupo: `G01M`
- Ciclo: `2026-I`
- Lote: `LT-JAB-2026-01`

### Formula de ejemplo

- Codigo: `REC-JAB-001`
- Nombre: `Formula base de jabon liquido citrico`

### Insumos requeridos por formula

| Insumo | Codigo | Cantidad teorica | Unidad |
|---|---|---:|---|
| Agua destilada | `AGUA-DEST-001` | 500 | ml |
| Glicerina liquida | `GLIC-001` | 120 | ml |
| Esencia citrica | `ESEN-CIT-001` | 20 | ml |

### Lotes demo esperados

| Insumo | Lote | Cantidad inicial sugerida | Vencimiento aproximado |
|---|---|---:|---|
| Agua destilada | `DEMO-AGUA-001` | 2000 ml | hoy + 180 dias |
| Glicerina liquida | `DEMO-GLIC-001` | 600 ml | hoy + 150 dias |
| Esencia citrica | `DEMO-ESEN-001` | 200 ml | hoy + 90 dias |

## Preparacion previa

Antes de la demo conviene validar:

1. que PostgreSQL este levantado;
2. que el backend arranque sin errores de Flyway;
3. que el frontend abra correctamente;
4. que el seed demo este activo;
5. que se pueda iniciar sesion con `admin`.

## Rutas principales a usar en la demo

1. `/manufactured-products`
2. `/recipes`
3. `/production`
4. `/movements`
5. `/inventory`
6. `/batches`

## Guion corto para la demo

Si quieres mostrar el flujo en pocos minutos, este es el orden recomendado:

1. abrir `Productos a elaborar`;
2. mostrar el producto de ejemplo con `grupo`, `ciclo` y `lote`;
3. abrir `Formulas`;
4. mostrar la formula teorica y sus insumos;
5. abrir `Elaboracion`;
6. preparar la elaboracion;
7. ajustar una cantidad real dentro del 10%;
8. confirmar la elaboracion;
9. mostrar el movimiento generado;
10. imprimir `Formula`;
11. imprimir `Descargo`;
12. abrir `Movimientos` y mostrar la trazabilidad.

## Flujo completo paso a paso

### 1. Verificar producto a elaborar

1. Ir a `Productos a elaborar`.
2. Confirmar que existe `ELAB-JAB-001 - Jabon liquido citrico`.
3. Validar que se muestran:
   - `Grupo G01M`
   - `Ciclo 2026-I`
   - `Lote LT-JAB-2026-01`

### 2. Verificar formula teorica

1. Ir a `Formulas`.
2. Abrir `REC-JAB-001 - Formula base de jabon liquido citrico`.
3. Confirmar que la formula esta asociada al producto correcto.
4. Verificar que aparecen los tres insumos del ejemplo.
5. Verificar que las cantidades teoricas coinciden con la tabla base.

### 3. Verificar stock y lotes

1. Ir a `Inventario` o `Lotes`.
2. Confirmar que existen lotes con disponibilidad para:
   - `AGUA-DEST-001`
   - `GLIC-001`
   - `ESEN-CIT-001`
3. Confirmar que hay suficiente stock para ejecutar una elaboracion completa.

### 4. Preparar elaboracion

1. Ir a `Elaboracion`.
2. Seleccionar:
   - Producto a elaborar: `Jabon liquido citrico`
   - Formula: `REC-JAB-001`
   - Laboratorio: `LAB-DEMO`
3. Opcional: registrar grupo visible como `Grupo 01 - Demo`.
4. Pulsar `Preparar`.

### 5. Validar previsualizacion

En la previsualizacion deben mostrarse:

1. producto a elaborar;
2. formula asociada;
3. laboratorio;
4. creador y fecha;
5. insumos requeridos;
6. cantidad teorica por insumo;
7. stock disponible;
8. lote sugerido;
9. campo editable de `cantidad real`;
10. rango permitido por la regla del `10%`.

## Ejemplo principal de prueba

### Cantidades teoricas

| Insumo | Cantidad teorica |
|---|---:|
| Agua destilada | 500 ml |
| Glicerina liquida | 120 ml |
| Esencia citrica | 20 ml |

### Cantidades reales sugeridas para la demo

Estas cantidades permiten mostrar el ajuste real sin violar la tolerancia:

| Insumo | Teorica | Real sugerida | Variacion |
|---|---:|---:|---:|
| Agua destilada | 500 | 520 | 4.00% |
| Glicerina liquida | 120 | 126 | 5.00% |
| Esencia citrica | 20 | 19 | 5.00% |

### Que mostrar en pantalla

1. editar la cantidad real de cada insumo;
2. observar que el estado visual sigue en tolerancia;
3. verificar que no hay alerta de exceso de variacion;
4. confirmar la elaboracion.

## Resultado esperado al confirmar

Al confirmar deben ocurrir estas salidas:

1. la elaboracion cambia a estado `Confirmada`;
2. se genera un `inventoryMovementId`;
3. el backend registra un movimiento `EXIT`;
4. las cantidades teoricas no se alteran en la formula;
5. el movimiento queda ligado a la elaboracion;
6. las cantidades reales quedan reflejadas en el descargo imprimible.

## Verificacion posterior

### Inventario

1. Ir a `Inventario`.
2. Buscar los tres insumos.
3. Confirmar que el stock disminuyo segun la suma real descargada.

### Lotes

1. Ir a `Lotes`.
2. Confirmar que los lotes sugeridos redujeron su disponibilidad.

### Movimientos

1. Ir a `Movimientos`.
2. Filtrar por laboratorio `LAB-DEMO`.
3. Ubicar el `EXIT` recien generado.
4. Verificar:
   - fecha;
   - usuario;
   - observaciones;
   - lineas por insumo;
   - lotes involucrados.

## Impresion

### Formula teorica

Desde la vista de elaboracion preparada:

1. pulsar `Imprimir formula`;
2. verificar encabezado;
3. verificar producto, grupo, ciclo y lote;
4. verificar lista completa de materias primas;
5. verificar cantidades teoricas.

### Descargo real

Despues de confirmar:

1. pulsar `Imprimir descargo`;
2. verificar distintivo visual o marca de control;
3. verificar producto, grupo, ciclo, lote y laboratorio;
4. verificar cantidades teoricas y reales;
5. verificar detalle por lote;
6. verificar responsable y confirmador.

## Casos de control recomendados

### Caso 1: variacion valida

Objetivo:

- demostrar que el sistema acepta ajustes reales razonables.

Prueba:

- usar las cantidades del ejemplo principal.

Resultado esperado:

- la elaboracion se confirma.

### Caso 2: variacion invalida

Objetivo:

- demostrar que el backend bloquea desviaciones fuera del 10%.

Prueba sugerida:

| Insumo | Teorica | Real invalida | Variacion |
|---|---:|---:|---:|
| Agua destilada | 500 | 560 | 12.00% |

Resultado esperado:

1. la UI marca el valor fuera de tolerancia;
2. si aun se intenta confirmar, el backend rechaza la operacion.

### Caso 3: cantidad real mayor al stock

Objetivo:

- demostrar que el sistema no permite descargar mas de lo disponible.

Resultado esperado:

1. la UI muestra advertencia;
2. la confirmacion queda bloqueada.

### Caso 4: integridad de formula usada

Objetivo:

- demostrar que una formula usada no se altera silenciosamente.

Prueba:

1. confirmar una elaboracion;
2. volver a `Formulas`;
3. intentar quitar un insumo de esa formula.

Resultado esperado:

- backend rechaza el cambio estructural.

## Reversion para cerrar la demo

Si quieres dejar la base en un estado controlado despues de la presentacion:

1. abrir `Movimientos`;
2. ubicar el movimiento generado por la elaboracion;
3. aplicar `Reversion`;
4. registrar un motivo;
5. validar que el stock vuelve al valor anterior.

## Checklist final para el presentador

- login funciona;
- producto a elaborar visible con trazabilidad academica;
- formula teorica visible con insumos correctos;
- lotes con stock disponibles;
- previsualizacion de elaboracion sin errores;
- cantidades reales editables;
- validacion del 10% visible;
- confirmacion genera movimiento;
- impresion de formula funciona;
- impresion de descargo funciona;
- movimiento aparece en historial;
- reversion disponible.

## Nota final

Este flujo ya permite una demo funcional completa del nuevo ciclo de `Formula` y `Elaboracion`.

Lo que queda fuera de este documento son decisiones futuras sobre:

1. restriccion fuerte por grupo;
2. roles academicos finales de `alumno` y `profesor`;
3. autorizaciones especiales del profesor para cambios excepcionales.
