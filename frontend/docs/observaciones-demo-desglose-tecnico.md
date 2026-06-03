# Desglose tecnico de observaciones de demo

Fecha de elaboracion: `2026-05-27`
Fuente principal: `Observaciones demo qyf sistema de inventario.docx`
Documento relacionado: [recipe-based-discharge-validation.md](C:/Users/jcriv/Documents/ues-qyf-inventory-system/frontend/docs/recipe-based-discharge-validation.md)

## Actualizacion por respuestas recibidas

Fecha de confirmacion funcional: `2026-06-02`
Fuente de confirmacion: `Respuesta Observaciones.docx`

### Definiciones ya confirmadas

- El termino funcional correcto pasa a ser `Producto a elaborar`.
- El termino funcional correcto pasa a ser `Formula` o `Formula Maestra`.
- El profesor crea el registro inicial del producto a elaborar.
- El grupo de estudiantes crea la formula teorica asociada al producto.
- Un mismo producto puede tener formulas distintas segun `grupo` y `ciclo`.
- El descargo real registra cantidades reales consumidas sobre la base de la formula teorica.
- La variacion de cantidad real respecto a la teorica no deberia superar `10%`.
- Agregar nuevas materias primas o pedir cantidades extra por dano, desperdicio o ajuste experimental requiere autorizacion del profesor.
- `grupo`, `ciclo` y `numero de lote` quedan confirmados como datos necesarios de trazabilidad.
- Se requieren dos impresiones:
  - formula teorica antes del laboratorio;
  - descargo real despues del laboratorio.
- La impresion final del descargo deberia incluir marca de agua o algun distintivo antifalsificacion.

### Punto abierto restante

- La restriccion por grupo sigue siendo deseable, pero depende de definir la estrategia de autenticacion y asignacion de usuarios por grupo.

## Objetivo

Separar las observaciones de demo en historias tecnicas ejecutables antes de implementar cambios sobre el modulo de descargo por receta.

## Alcance de este documento

Este desglose cubre solo la `Prioridad alta` detectada en el backlog funcional:

- definicion funcional del modelo y terminologia;
- trazabilidad academica del producto;
- flujo de formula teorica;
- flujo de descargo real;
- restricciones por rol;
- impresion de formula y descargo.

## 1. Decisiones funcionales por confirmar

Estas decisiones ya fueron respondidas en su mayor parte. Solo queda abierto el modelo de acceso por grupo.

### DF-01 Terminologia oficial del modulo

- Decision confirmada: el dominio funcional debe usar `Producto a elaborar` y `Formula`.
- Impacto:
  - textos de UI;
  - nombres de campos;
  - nombres de endpoints o DTOs si se decide alinear backend;
  - documentacion y manuales.
- Estado: `cerrado`.

### DF-02 Responsable de crear la formula

- Decision confirmada:
  - el profesor crea el `producto a elaborar`;
  - el grupo de estudiantes crea la `formula teorica`.
- Impacto:
  - permisos por rol;
  - auditoria de creador y editor;
  - flujo de aprobacion.
- Estado: `cerrado`.

### DF-03 Relacion entre producto y formula

- Decision confirmada: un mismo producto puede tener formulas distintas segun `grupo` y `ciclo`.
- Impacto:
  - unicidad de codigo;
  - necesidad de versionado;
  - reglas para editar una formula ya usada.
- Estado: `cerrado`.

### DF-04 Regla del descargo real

- Decision confirmada:
  - el descargo real registra cantidades realmente utilizadas;
  - la cantidad real puede variar respecto a la teorica;
  - la variacion esperada no deberia superar `10%`;
  - agregar una materia prima nueva o pedir mas cantidad requiere autorizacion del profesor.
- Estado: `cerrado`.

### DF-05 Restriccion por grupo y autenticacion

- Punto pendiente: se desea que cada grupo solo interactue con su producto y su formula, pero no se definio aun si eso se resolvera con usuarios por grupo, roles adicionales o algun filtro operativo.
- Impacto:
  - login y gestion de credenciales;
  - asignacion de grupo a usuario;
  - filtros de consulta y permisos en backend/frontend.
- Estado: `abierto`.

## 2. Historias tecnicas de backend

### BE-01 Extender entidad de producto con trazabilidad academica

- Objetivo: agregar `grupo`, `ciclo` y `numeroLote` al producto a elaborar.
- Cambios esperados:
  - migracion Flyway;
  - entidad y repositorio;
  - DTOs de alta, edicion y detalle;
  - validaciones de formato y obligatoriedad.
- Criterios de aceptacion:
  - persiste `grupo`, `ciclo` y `numeroLote`;
  - los campos retornan en listados y detalle;
  - existe validacion de formato basica para lote.

### BE-02 Separar formula teorica de ejecucion real

- Objetivo: evitar que la formula teorica y el consumo real sean el mismo registro logico.
- Cambios esperados:
  - modelo para formula teorica;
  - modelo para ejecucion o descargo real asociado;
  - relacion clara entre producto, formula y movimiento.
- Criterios de aceptacion:
  - una ejecucion referencia una formula base;
  - las cantidades reales no sobrescriben la formula teorica;
  - la trazabilidad se conserva despues de confirmar y revertir.

### BE-02A Validacion de variacion maxima sobre cantidades reales

- Objetivo: controlar que la cantidad real no se aleje indebidamente de la teorica.
- Cambios esperados:
  - regla de validacion sobre cada linea de descargo;
  - mensaje claro cuando la variacion supere el `10%`;
  - decision tecnica de si el exceso bloquea, advierte o exige autorizacion.
- Criterios de aceptacion:
  - el backend calcula la desviacion porcentual por materia prima;
  - existe respuesta consistente cuando se supera el umbral definido.

### BE-03 Reglas de autorizacion por rol

- Objetivo: limitar acciones segun `alumno` y `profesor`.
- Cambios esperados:
  - policy o validacion de autorizacion;
  - validacion de backend para impedir cambios estructurales no autorizados;
  - auditoria minima de quien creo o modifico.
- Criterios de aceptacion:
  - alumno no puede agregar/quitar materias primas fuera de la regla aprobada;
  - profesor si puede corregir estructura;
  - backend rechaza operaciones invalidas aunque la UI falle.

### BE-04 Soporte de impresion o exportacion

- Objetivo: exponer datos listos para generar impresion de formula y descargo.
- Cambios esperados:
  - endpoint de detalle imprimible o payload de reporte;
  - estructura con encabezado, materias primas y cantidades;
  - inclusion de grupo, ciclo, lote, producto y responsable.
- Criterios de aceptacion:
  - existe una salida consistente para formula teorica;
  - existe una salida consistente para descargo real.
  - la salida de descargo final permite incluir distintivo o marca de agua.

### BE-05 Reglas de integridad sobre formulas ya usadas

- Objetivo: evitar inconsistencias si una formula ya fue usada en laboratorio.
- Cambios esperados:
  - bloqueo de edicion destructiva o versionado;
  - pruebas de integridad para movimientos existentes.
- Criterios de aceptacion:
  - no se pierde trazabilidad historica;
  - una formula usada no queda alterada silenciosamente.

## 3. Historias tecnicas de frontend

### FE-01 Ajustar terminologia visible

- Objetivo: alinear textos, titulos, labels y acciones con la terminologia aprobada.
- Cambios esperados:
  - menus;
  - encabezados;
  - formularios;
  - mensajes de exito/error;
  - breadcrumbs si existen.
- Criterios de aceptacion:
  - la UI usa una terminologia consistente de punta a punta.

### FE-02 Formulario de producto con trazabilidad academica

- Objetivo: capturar `grupo`, `ciclo` y `numero de lote`.
- Cambios esperados:
  - nuevos campos en alta y edicion;
  - validaciones de longitud y formato;
  - visualizacion en detalle o tabla.
- Criterios de aceptacion:
  - los campos son editables donde corresponde;
  - quedan visibles al seleccionar el producto.

### FE-03 Flujo de formula teorica para alumno

- Objetivo: permitir que el alumno arme su formula teorica a partir del producto seleccionado.
- Cambios esperados:
  - seleccion de producto;
  - listado editable de materias primas;
  - cantidades teoricas;
  - resumen final de formula.
- Criterios de aceptacion:
  - el usuario puede crear la formula completa sin salir del flujo;
  - el resumen muestra todas las materias primas con sus cantidades.

### FE-04 Restriccion visual por rol

- Objetivo: ocultar o deshabilitar acciones no permitidas para alumno.
- Cambios esperados:
  - botones de agregar/quitar materia prima;
  - accion de editar estructura;
  - mensajes claros cuando una accion requiere profesor.
- Criterios de aceptacion:
  - la UI no invita al alumno a ejecutar acciones prohibidas.

### FE-05 Descargo real post-laboratorio

- Objetivo: registrar cantidades reales consumidas a partir de la formula base.
- Cambios esperados:
  - pantalla o seccion de ajuste de cantidades;
  - diferencia visible entre valor teorico y real;
  - validacion especial si se intenta agregar o quitar materia prima.
- Criterios de aceptacion:
  - el usuario distingue claramente formula prevista vs consumo real;
  - los casos especiales disparan la regla de autorizacion definida.
  - la UI muestra cuando una variacion supera el `10%`.

### FE-06 Impresion de formula y descargo

- Objetivo: generar una vista lista para imprimir desde frontend o consumir un reporte backend.
- Cambios esperados:
  - boton `Imprimir formula`;
  - boton `Imprimir descargo`;
  - layout legible para papel o PDF.
- Criterios de aceptacion:
  - ambos documentos incluyen encabezado y detalle completo;
  - la impresion no omite materias primas ni cantidades.
  - el descargo final puede llevar marca de agua o distintivo visible.

## 4. Permisos y seguridad

### PS-01 Matriz minima de permisos

- `Alumno`
  - crear formula teorica del producto asignado a su grupo, cuando esa restriccion exista;
  - ajustar cantidades reales durante el descargo;
  - imprimir formula y descargo si esa regla es aprobada.
- `Profesor`
  - corregir estructura de formula;
  - autorizar cambios de materias primas;
  - revisar e imprimir soportes finales.

### PS-02 Validacion cruzada backend/frontend

- Regla: toda restriccion visible en UI debe existir tambien en backend.
- Criterio de cierre: pruebas cubren intentos de acceso indebido por API.

### PS-03 Estrategia pendiente para restriccion por grupo

- Alternativas a decidir:
  - usuario individual por grupo;
  - usuario individual por estudiante con pertenencia a grupo;
  - filtro operativo sin restriccion fuerte de autenticacion.
- Criterio de cierre: el encargado define el nivel real de control requerido.

## 5. Impresion y reportes

### RP-01 Formato de formula teorica

- Contenido minimo:
  - producto;
  - grupo;
  - ciclo;
  - lote;
  - fecha;
  - listado de materias primas;
  - cantidades teoricas.

### RP-02 Formato de descargo real

- Contenido minimo:
  - producto;
  - grupo;
  - ciclo;
  - lote;
  - fecha de laboratorio;
  - materias primas;
  - cantidad teorica;
  - cantidad real;
  - responsable y autorizacion del profesor si aplica.
  - marca de agua o distintivo visual de control.

## 6. Orden recomendado de ejecucion

1. Alinear terminologia en modelo, endpoints y UI segun `DF-01`.
2. Implementar trazabilidad academica del producto con `BE-01` y `FE-02`.
3. Separar formula teorica y descargo real con `BE-02` y `FE-05`.
4. Implementar permisos base y autorizaciones con `BE-03`, `FE-04` y `PS-01`.
5. Implementar control de variacion de cantidades con `BE-02A` y `FE-05`.
6. Implementar salidas de impresion con `BE-04`, `FE-06`, `RP-01` y `RP-02`.
7. Proteger historial y formulas usadas con `BE-05`.
8. Definir si se implementa restriccion fuerte por grupo con `DF-05` y `PS-03`.
9. Repetir la validacion funcional completa.

## 7. Secuencia concreta de trabajo

### Backend

1. Crear migraciones Flyway para `grupo`, `ciclo` y `numero_lote` en el producto a elaborar.
2. Ajustar entidades, DTOs, mappers y validaciones para la nueva trazabilidad.
3. Renombrar conceptos expuestos a `formula` donde aplique sin romper compatibilidad innecesaria.
4. Separar persistencia de formula teorica y descargo real.
5. Agregar validacion porcentual de cantidad real contra cantidad teorica.
6. Implementar autorizacion del profesor para agregar materias primas nuevas o pedir cantidades extra.
7. Preparar payloads o endpoints para impresion de formula y descargo con datos completos.
8. Proteger integridad historica de formulas ya utilizadas.

### Frontend

1. Actualizar textos de menu, pantallas y formularios a `Producto a elaborar` y `Formula`.
2. Agregar campos `grupo`, `ciclo` y `numero de lote` en alta, edicion y detalle.
3. Ajustar el flujo para que el profesor cree el producto y el estudiante cargue la formula teorica.
4. Mostrar comparativo entre cantidad teorica y cantidad real en el descargo.
5. Mostrar alerta o bloqueo cuando la variacion supere el `10%`, segun la regla tecnica que se adopte.
6. Ocultar o deshabilitar acciones estructurales para alumno cuando requieran profesor.
7. Agregar impresion de formula teorica y de descargo final con layout legible.
8. Dejar preparado el flujo para futura restriccion por grupo si el encargado la aprueba a nivel de autenticacion.

## 8. Riesgos

- Cambiar terminologia sin cambiar modelo puede dejar una UI incoherente.
- Permitir edicion directa de formulas usadas puede romper trazabilidad historica.
- Mezclar formula teorica y consumo real en el mismo registro puede impedir auditoria.
- Implementar impresion antes de fijar el modelo puede duplicar trabajo.
- Implementar restriccion por grupo sin definir autenticacion puede dejar seguridad aparente pero no real.

## 9. Resultado esperado

Al cerrar este documento como base tecnica, el equipo deberia poder estimar cada bloque por separado y decidir si el siguiente paso es:

- una iteracion de backend para modelo y validaciones;
- una iteracion de frontend para flujos y formularios;
- y una decision aparte sobre autenticacion y restriccion por grupo.
