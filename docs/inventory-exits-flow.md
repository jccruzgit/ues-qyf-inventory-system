# Flujo de registro de salidas y descargos

## Descripcion del flujo

El modulo de salidas o descargos registra una operacion normal de inventario para reducir existencias disponibles en un laboratorio. El flujo permite:

1. seleccionar laboratorio;
2. seleccionar producto;
3. consultar existencias disponibles para ese producto en el laboratorio;
4. seleccionar un lote disponible o la existencia sin lote cuando aplique;
5. registrar cantidad, observacion general y observacion por linea;
6. guardar el movimiento como `EXIT`.

La operacion reutiliza el endpoint `POST /api/inventory-movements` ya existente, enviando `movementType = EXIT`.

## Diferencia entre salida y reversion

- **Salida o descargo**: movimiento normal de consumo, entrega, descarte o baja operativa. Reduce stock y queda en historial como parte regular de la trazabilidad.
- **Reversion**: movimiento correctivo compensatorio sobre un movimiento previo. No edita ni elimina el movimiento original; crea un nuevo movimiento relacionado para deshacer completamente su efecto.

La implementacion de salidas no modifica el flujo de reversion existente.

## Reglas de validacion

1. El laboratorio es obligatorio.
2. El producto es obligatorio.
3. La cantidad a descargar debe ser mayor que cero.
4. Debe seleccionarse un lote o existencia disponible cuando existan opciones con stock.
5. No puede registrarse una salida si el producto no tiene stock disponible en el laboratorio seleccionado.
6. No puede descargarse una cantidad mayor al stock disponible del lote seleccionado.
7. El backend vuelve a validar stock insuficiente tanto a nivel de producto como a nivel de lote.
8. El movimiento se registra en historial como `EXIT`.
9. La salida puede reversarse despues usando el flujo de reversion ya implementado.

## Pasos de prueba manual

1. Crear un producto o reutilizar uno existente con unidad base configurada.
2. Registrar una entrada para ese producto en un laboratorio y lote determinados.
3. Verificar en inventario que el stock y el lote aparecen disponibles.
4. Abrir **Registrar salida** desde la barra lateral o desde el modulo de inventario.
5. Seleccionar el mismo laboratorio, producto y lote.
6. Registrar una salida parcial con cantidad menor al stock disponible.
7. Confirmar que el inventario disminuye en la vista de stock.
8. Abrir historial de movimientos y verificar que aparece el movimiento `EXIT`.
9. Reversar la salida desde el historial usando el flujo ya existente.
10. Verificar que el stock vuelve a incrementarse y que la reversion queda trazada en historial.
