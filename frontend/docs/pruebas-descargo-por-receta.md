# Pruebas del flujo de descargo por receta

## Objetivo

Validar que el modulo de `Elaboracion` en la ruta `/production`:

- permita preparar una elaboracion basada en receta;
- muestre insumos, lotes sugeridos y advertencias;
- confirme la salida solo cuando exista stock suficiente;
- genere trazabilidad hacia el modulo de movimientos.

## Ruta a probar

- Frontend: `/production`
- Acceso alterno esperado: menu lateral `Elaboracion`

## Prerrequisitos

Antes de iniciar, confirme lo siguiente:

1. Existe al menos un `producto elaborado` activo.
2. Existe al menos una `receta` activa asociada a ese producto elaborado.
3. La receta tiene al menos un insumo.
4. Existe al menos un `laboratorio` disponible para el usuario autenticado.
5. Existe stock en inventario para al menos un escenario con:
   - stock suficiente;
   - stock insuficiente;
   - multiples lotes para validar sugerencia FEFO.
6. El usuario tiene permisos para registrar elaboraciones.

## Datos sugeridos

Prepare estos datos de prueba:

- Producto elaborado A con una sola receta activa.
- Producto elaborado B con dos o mas recetas activas.
- Receta con todos los insumos disponibles.
- Receta con al menos un insumo sin disponibilidad suficiente.
- Lotes con distinta fecha de vencimiento para comprobar FEFO.

## Caso 1: apertura de pantalla

### Pasos

1. Inicie sesion.
2. Abra la ruta `/production`.

### Resultado esperado

- Se visualiza el encabezado `Elaboracion`.
- Se muestra el bloque informativo `Descargo por receta`.
- Se muestran los campos:
  - `Producto elaborado`
  - `Receta`
  - `Laboratorio`
  - `Grupo o estudiante`
  - `Observaciones`
- Se muestra el estado vacio de previsualizacion.
- El boton superior `Descargo individual` redirige a `/inventory/exits/new`.

## Caso 2: validaciones obligatorias del formulario

### Pasos

1. Entre a `/production`.
2. Sin seleccionar ningun valor, pulse `Preparar`.

### Resultado esperado

- El formulario muestra errores para:
  - `Producto elaborado`
  - `Receta`
  - `Laboratorio`
- No se genera previsualizacion.

## Caso 3: dependencia entre producto elaborado y receta

### Pasos

1. Seleccione un `Producto elaborado` que tenga recetas activas.
2. Revise las opciones del campo `Receta`.
3. Cambie a otro producto elaborado con un conjunto distinto de recetas.

### Resultado esperado

- El campo `Receta` permanece bloqueado hasta seleccionar producto elaborado.
- Al elegir producto elaborado, el campo `Receta` solo muestra recetas asociadas a ese producto.
- Si el producto nuevo no comparte la receta anterior, la seleccion de receta se limpia.
- Si el producto solo tiene una receta valida, el sistema puede autoseleccionarla.

## Caso 4: preparacion exitosa con stock suficiente

### Pasos

1. Seleccione:
   - un `Producto elaborado`;
   - una `Receta` con stock suficiente;
   - un `Laboratorio`.
2. Opcionalmente ingrese `Grupo o estudiante`.
3. Opcionalmente ingrese `Observaciones`.
4. Pulse `Preparar`.

### Resultado esperado

- Se muestra mensaje de exito indicando que la previsualizacion fue generada.
- En el panel derecho aparece estado `Borrador`.
- Se muestra el resumen de:
  - producto elaborado;
  - receta;
  - laboratorio;
  - usuario creador;
  - fecha de creacion;
  - grupo, si fue ingresado;
  - observaciones, si fueron ingresadas.
- Se listan los insumos requeridos.
- Cada insumo muestra:
  - codigo;
  - cantidad requerida;
  - ubicacion;
  - disponible total;
  - observaciones;
  - lotes sugeridos.
- Se muestra advertencia amarilla indicando que al confirmar se descargaran todos los insumos listados.
- El boton `Confirmar elaboracion` queda habilitado.

## Caso 5: sugerencia FEFO por lotes

### Pasos

1. Use una receta cuyos insumos tengan mas de un lote disponible.
2. Pulse `Preparar`.
3. Revise el orden de lotes sugeridos en la previsualizacion.

### Resultado esperado

- El sistema propone lotes para cada insumo.
- Los lotes sugeridos respetan FEFO, priorizando los de vencimiento mas cercano.
- La suma de cantidades sugeridas cubre la cantidad requerida cuando existe stock suficiente.

## Caso 6: preparacion con stock insuficiente

### Pasos

1. Seleccione una receta con al menos un insumo insuficiente.
2. Pulse `Preparar`.

### Resultado esperado

- Se genera la previsualizacion.
- Se muestra alerta roja indicando falta de stock.
- Los insumos sin disponibilidad suficiente aparecen marcados como `Stock insuficiente`.
- El resumen del error indica producto, cantidad requerida y cantidad disponible.
- El boton `Confirmar elaboracion` queda deshabilitado.

## Caso 7: confirmacion exitosa

### Pasos

1. Prepare una elaboracion con stock suficiente.
2. Pulse `Confirmar elaboracion`.

### Resultado esperado

- Se muestra mensaje de confirmacion exitosa.
- El estado cambia de `Borrador` a `Confirmada`.
- Se muestra el numero de movimiento de inventario generado.
- El boton de confirmacion queda deshabilitado luego de confirmar.
- La seccion `Movimiento asociado` permite navegar a `/movements`.

## Caso 8: trazabilidad en movimientos

### Pasos

1. Desde una elaboracion confirmada, pulse `Ver movimientos`.
2. Busque el movimiento generado.

### Resultado esperado

- Existe un movimiento de salida relacionado con la elaboracion confirmada.
- El movimiento corresponde al laboratorio usado en la elaboracion.
- Debe existir relacion trazable con la receta o el `productionRunId`.
- Las cantidades descargadas coinciden con lo mostrado en la confirmacion.

## Caso 9: reinicio del formulario

### Pasos

1. Complete varios campos.
2. Pulse `Reiniciar`.

### Resultado esperado

- Se limpian `Producto elaborado`, `Receta`, `Laboratorio`, `Grupo o estudiante` y `Observaciones`.
- La previsualizacion desaparece.
- Se limpian mensajes de error y mensajes de exito.

## Caso 10: cambio de datos despues de preparar

### Pasos

1. Prepare una elaboracion.
2. Cambie cualquiera de estos campos:
   - `Producto elaborado`
   - `Receta`
   - `Laboratorio`
   - `Grupo o estudiante`
   - `Observaciones`

### Resultado esperado

- La previsualizacion anterior se invalida.
- El sistema limpia el estado previo para obligar a preparar nuevamente.

## Caso 11: limites de texto

### Pasos

1. Ingrese en `Grupo o estudiante` mas de 150 caracteres.
2. Ingrese en `Observaciones` mas de 500 caracteres.

### Resultado esperado

- El formulario bloquea el envio.
- Se muestran mensajes de validacion acordes al limite configurado.

## Caso 12: errores funcionales esperados

### Escenarios a probar

1. Receta eliminada o no disponible al momento de preparar.
2. Laboratorio sin acceso para el usuario.
3. Receta sin insumos.
4. Intento de confirmar una elaboracion ya confirmada.
5. Sesion expirada o usuario sin permisos.

### Resultado esperado

- El sistema muestra mensajes claros y no rompe la pantalla.
- No se crean movimientos parciales.
- No se permite confirmar si el estado del backend ya no es valido.

## Checklist rapido de aprobacion

Marque como aprobado cuando se cumpla todo lo siguiente:

- [ ] La pantalla carga correctamente.
- [ ] Las validaciones obligatorias funcionan.
- [ ] La receta depende del producto elaborado.
- [ ] La previsualizacion muestra insumos y lotes sugeridos.
- [ ] El flujo con stock suficiente permite confirmar.
- [ ] El flujo con stock insuficiente bloquea la confirmacion.
- [ ] La confirmacion genera movimiento de inventario.
- [ ] Existe trazabilidad hacia el historial de movimientos.
- [ ] El boton `Reiniciar` limpia el estado completo.
- [ ] Los cambios en el formulario invalidan la previsualizacion previa.

## Evidencia recomendada

Guarde evidencia minima de estos puntos:

1. Pantalla inicial vacia.
2. Error por campos obligatorios.
3. Previsualizacion con stock suficiente.
4. Previsualizacion con stock insuficiente.
5. Confirmacion exitosa con numero de movimiento.
6. Registro del movimiento en `/movements`.
