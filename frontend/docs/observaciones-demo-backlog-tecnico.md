# Backlog tecnico de observaciones de demo

Fecha de elaboracion: `2026-06-02`
Documento base: [observaciones-demo-desglose-tecnico.md](C:/Users/jcriv/Documents/ues-qyf-inventory-system/frontend/docs/observaciones-demo-desglose-tecnico.md)

## Objetivo

Traducir el desglose tecnico en tickets implementables, pequenos y ordenados por dependencia.

## Convenciones

- `BE`: backend
- `FE`: frontend
- `QA`: validacion
- `OPEN`: pendiente de definicion externa

## Orden de ejecucion recomendado

1. `BE-01` a `BE-04`
2. `FE-01` a `FE-03`
3. `BE-05` a `BE-07`
4. `FE-04` a `FE-07`
5. `BE-08`
6. `QA-01` a `QA-03`
7. `OPEN-01`

## Tickets backend

### BE-01 Renombrar terminologia de dominio expuesta

- Objetivo: alinear contratos y textos expuestos con `Producto a elaborar` y `Formula`.
- Alcance:
  - revisar DTOs, responses y nombres visibles en API;
  - definir si el renombre es solo de capa de presentacion o tambien de clases/paths.
- Dependencias: ninguna.
- Criterio de aceptacion: la API y la documentacion visible no mezclan `receta` con `formula` en el flujo nuevo.

### BE-02 Agregar trazabilidad academica al producto

- Objetivo: persistir `grupo`, `ciclo` y `numeroLote`.
- Alcance:
  - migracion Flyway;
  - entidad;
  - DTOs de crear, editar y listar;
  - validaciones.
- Dependencias: ninguna.
- Criterio de aceptacion: los tres campos se guardan y retornan correctamente.

### BE-03 Soportar multiples formulas por producto segun grupo y ciclo

- Objetivo: permitir que un mismo producto tenga formulas distintas.
- Alcance:
  - revisar unicidad actual;
  - definir relacion producto-formula;
  - evitar colisiones por grupo/ciclo.
- Dependencias: `BE-02`.
- Criterio de aceptacion: dos grupos distintos pueden registrar formulas diferentes para el mismo producto.

### BE-04 Separar formula teorica de descargo real

- Objetivo: evitar que consumo real sobrescriba la formula base.
- Alcance:
  - nuevo modelo o ajuste de modelo actual;
  - relacion entre formula, ejecucion y movimiento;
  - persistencia de cantidades teoricas y reales.
- Dependencias: `BE-03`.
- Criterio de aceptacion: la formula teorica queda intacta despues del descargo real.

### BE-05 Validar variacion maxima de cantidades

- Objetivo: controlar desviacion de `cantidadReal` contra `cantidadTeorica`.
- Alcance:
  - regla porcentual del `10%`;
  - mensajes de error o advertencia;
  - decision tecnica de bloqueo o autorizacion.
- Dependencias: `BE-04`.
- Criterio de aceptacion: el backend detecta y responde consistentemente ante variaciones mayores al umbral.

### BE-06 Autorizar cambios especiales por profesor

- Objetivo: restringir agregar materia prima nueva o pedir cantidades extra.
- Alcance:
  - validacion por rol;
  - registro de autorizacion;
  - rechazo de operaciones no autorizadas.
- Dependencias: `BE-04`.
- Criterio de aceptacion: el alumno no puede ejecutar cambios estructurales sin autorizacion valida.

### BE-07 Exponer datos para impresion de formula y descargo

- Objetivo: entregar payloads listos para impresion.
- Alcance:
  - salida de formula teorica;
  - salida de descargo real;
  - inclusion de grupo, ciclo, lote y responsables.
- Dependencias: `BE-02`, `BE-04`.
- Criterio de aceptacion: existe respuesta completa y estable para ambas impresiones.

### BE-08 Proteger historial de formulas usadas

- Objetivo: evitar alteracion silenciosa de formulas ya utilizadas.
- Alcance:
  - bloqueo de edicion destructiva o versionado;
  - pruebas de integridad historica.
- Dependencias: `BE-04`.
- Criterio de aceptacion: una formula usada en laboratorio mantiene trazabilidad historica.

## Tickets frontend

### FE-01 Renombrar UI a producto a elaborar y formula

- Objetivo: alinear menus, encabezados y labels.
- Alcance:
  - menu lateral;
  - titulos de pantalla;
  - formularios;
  - mensajes.
- Dependencias: `BE-01`.
- Criterio de aceptacion: la UI usa terminologia consistente en todo el flujo.

### FE-02 Agregar campos de trazabilidad academica

- Objetivo: capturar y mostrar `grupo`, `ciclo` y `numero de lote`.
- Alcance:
  - alta;
  - edicion;
  - detalle;
  - tablas si aplica.
- Dependencias: `BE-02`.
- Criterio de aceptacion: los campos quedan visibles y editables donde corresponde.

### FE-03 Ajustar flujo para profesor y estudiantes

- Objetivo: reflejar que el profesor crea el producto y el estudiante arma la formula teorica.
- Alcance:
  - estados de pantalla;
  - acciones disponibles por contexto;
  - mensajes orientados al actor correcto.
- Dependencias: `BE-03`.
- Criterio de aceptacion: el flujo principal respeta la secuencia funcional aprobada.

### FE-04 Mostrar formula teorica y descargo real por separado

- Objetivo: distinguir claramente lo planeado de lo consumido.
- Alcance:
  - comparativo teorico vs real;
  - tabla de materias primas;
  - resumen de diferencias.
- Dependencias: `BE-04`.
- Criterio de aceptacion: el usuario puede ver ambos valores sin ambiguedad.

### FE-05 Mostrar regla de variacion del 10%

- Objetivo: reflejar en UI el control de desviacion.
- Alcance:
  - alerta visual;
  - mensaje de bloqueo o advertencia;
  - ayuda contextual.
- Dependencias: `BE-05`.
- Criterio de aceptacion: el usuario entiende cuando una cantidad real excede el rango permitido.

### FE-06 Restringir acciones estructurales al profesor

- Objetivo: ocultar o deshabilitar acciones no permitidas para alumnos.
- Alcance:
  - agregar materia prima;
  - quitar materia prima;
  - solicitar cantidad extra;
  - mensajes de autorizacion.
- Dependencias: `BE-06`.
- Criterio de aceptacion: la UI no expone acciones prohibidas como si fueran normales.

### FE-07 Implementar impresion de formula y descargo

- Objetivo: permitir generar ambos documentos.
- Alcance:
  - accion `Imprimir formula`;
  - accion `Imprimir descargo`;
  - layout listo para papel o PDF;
  - distintivo visible en descargo final.
- Dependencias: `BE-07`.
- Criterio de aceptacion: ambas impresiones salen con la informacion completa esperada.

## Tickets de validacion

### QA-01 Probar trazabilidad completa del producto

- Objetivo: validar grupo, ciclo y lote de punta a punta.
- Dependencias: `BE-02`, `FE-02`.
- Criterio de aceptacion: los datos persisten, se muestran y llegan a impresion.

### QA-02 Probar formula teorica vs descargo real

- Objetivo: validar separacion de cantidades teoricas y reales.
- Dependencias: `BE-04`, `FE-04`, `FE-05`.
- Criterio de aceptacion: el descargo real no altera la formula teorica y respeta la regla del `10%`.

### QA-03 Probar permisos y autorizaciones

- Objetivo: validar restricciones de alumno y profesor.
- Dependencias: `BE-06`, `FE-06`.
- Criterio de aceptacion: los cambios especiales solo proceden con autorizacion valida.

## Punto abierto

### OPEN-01 Restriccion por grupo y autenticacion

- Objetivo: definir si cada grupo tendra usuario propio o si se aplicara otra estrategia.
- Impacto:
  - seguridad;
  - asignacion de productos;
  - filtros por grupo;
  - experiencia operativa del laboratorio.
- Estado: pendiente de decision del encargado.

## Corte sugerido por iteraciones

### Iteracion 1

- `BE-01`
- `BE-02`
- `BE-03`
- `FE-01`
- `FE-02`

### Iteracion 2

- `BE-04`
- `BE-05`
- `BE-06`
- `FE-03`
- `FE-04`
- `FE-05`
- `FE-06`

### Iteracion 3

- `BE-07`
- `BE-08`
- `FE-07`
- `QA-01`
- `QA-02`
- `QA-03`

## Resultado esperado

Este backlog debe permitir asignar trabajo sin volver al documento narrativo de observaciones y sin mezclar decisiones abiertas con implementacion ya definida.
