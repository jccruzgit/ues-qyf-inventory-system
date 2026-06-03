# Plan de ejecucion backend - Iteracion 1

Fecha de elaboracion: `2026-06-02`
Documento base: [observaciones-demo-backlog-tecnico.md](C:/Users/jcriv/Documents/ues-qyf-inventory-system/frontend/docs/observaciones-demo-backlog-tecnico.md)

## Objetivo

Ejecutar la primera iteracion de backend para soportar:

- terminologia expuesta alineada a `Producto a elaborar` y `Formula`;
- trazabilidad academica del producto con `grupo`, `ciclo` y `numeroLote`;
- multiples formulas por producto segun `grupo` y `ciclo`.

## Alcance

Este plan cubre:

- `BE-01` renombre de terminologia expuesta;
- `BE-02` trazabilidad academica del producto;
- `BE-03` multiples formulas por producto.

No cubre aun:

- separacion entre formula teorica y descargo real;
- autorizaciones por profesor;
- impresion;
- control del `10%`.

## Secuencia sugerida

### Paso 1. Inspeccion de modelo actual

- Ubicar entidades, DTOs, controladores, servicios y repositorios de:
  - `ManufacturedProduct`;
  - `Recipe`;
  - `ProductionRun`;
  - `InventoryMovement` si arrastra nombres visibles.
- Confirmar:
  - reglas actuales de unicidad;
  - relaciones entre producto y receta;
  - payloads ya consumidos por frontend.

Resultado esperado:

- mapa claro de clases y endpoints impactados.

### Paso 2. Definir estrategia de renombre

- Mantener nombres internos actuales si el cambio completo es demasiado grande para esta iteracion.
- Priorizar renombre en:
  - mensajes de validacion;
  - labels expuestos en DTOs/documentacion si aplica;
  - nombres de campos serializados adicionales si hace falta compatibilidad progresiva.

Decision sugerida:

- no renombrar paquetes o clases Java en esta iteracion si eso aumenta demasiado el riesgo;
- si el modelo interno sigue usando `Recipe` o `ManufacturedProduct`, alinear primero la capa expuesta.

### Paso 3. Implementar trazabilidad academica en producto

- Agregar en base de datos:
  - `group_code`;
  - `cycle`;
  - `lot_number`.
- Actualizar:
  - entidad;
  - request DTO de crear;
  - request DTO de editar;
  - response DTO de listar/detalle;
  - validaciones;
  - mapper o assembler.

Validaciones minimas sugeridas:

- obligatorios;
- maximo `50` caracteres;
- trim;
- lote no vacio.

Resultado esperado:

- el endpoint de productos ya acepta y devuelve esos tres campos.

### Paso 4. Revisar unicidad del producto

- Confirmar si la unicidad actual esta en:
  - `code`;
  - `name`;
  - otra combinacion.
- Decidir si en esta iteracion se mantiene `code` globalmente unico o si pasa a ser unico por `group_code` + `cycle`.

Recomendacion pragmatica:

- si cambiar unicidad rompe demasiados casos existentes, mantener `code` unico en esta iteracion y documentar la regla;
- mover la decision mas fina de unicidad a una iteracion posterior solo si el encargado no la exigio como bloqueo inmediato.

### Paso 5. Habilitar multiples formulas por producto

- Revisar si hoy existe restriccion tecnica de una sola receta por producto.
- Quitar esa restriccion si existe en:
  - servicio;
  - validadores;
  - repositorio;
  - indice unico de base de datos.

Objetivo funcional:

- permitir varias formulas para el mismo producto;
- distinguirlas al menos por `code`;
- dejar preparado el terreno para filtrar por grupo/ciclo luego.

### Paso 6. Enriquecer respuesta de formula con datos del producto

- Incluir en DTO de formula, si no existe ya:
  - `manufacturedProductGroupCode`;
  - `manufacturedProductCycle`;
  - `manufacturedProductLotNumber`.

Resultado esperado:

- frontend puede mostrar contexto de grupo/ciclo/lote sin llamadas extra.

### Paso 7. Ajustar mensajes y errores expuestos

- Reemplazar en mensajes visibles:
  - `recipe` por `formula`;
  - `manufactured product` por `producto a elaborar`, donde aplique en traduccion o mensaje de negocio.

Nota:

- si los mensajes base del backend siguen en ingles tecnico, al menos asegurar que frontend ya reciba campos compatibles y errores comprensibles.

### Paso 8. Pruebas backend minimas

- Crear o ajustar tests para:
  - crear producto con `grupo`, `ciclo`, `numeroLote`;
  - editar producto con esos campos;
  - listar y obtener detalle con esos campos;
  - crear dos formulas distintas para el mismo producto;
  - validar que no se rompe el flujo actual de `production-runs`.

## Archivos probables a tocar

Dependiendo de la estructura real del backend, buscar primero en:

- entidades de producto y formula;
- DTOs request/response;
- services de producto y formula;
- controllers REST;
- repositorios JPA;
- migraciones Flyway;
- tests de integracion o unitarios del modulo.

## Riesgos concretos

- cambiar unicidad sin revisar datos existentes puede romper inserts o migraciones;
- renombrar demasiado a nivel interno puede inflar el alcance sin valor inmediato;
- agregar campos al producto sin devolverlos en DTO deja al frontend inconsistente;
- permitir multiples formulas sin revisar el flujo de elaboracion puede exponer listas ambiguas si no se mantiene codigo claro.

## Criterio de cierre de Iteracion 1 backend

La iteracion backend puede considerarse cerrada cuando:

- el producto acepta y devuelve `grupo`, `ciclo` y `numeroLote`;
- una formula puede asociarse a un producto sin asumir unicidad uno a uno;
- los DTOs necesarios para frontend ya incluyen el contexto del producto;
- tests y build del backend pasan;
- no se rompe el flujo actual de elaboracion existente.
