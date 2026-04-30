# Actualizacion visual de demo

## Objetivo

Aplicar una presentacion institucional sobria para la rama `demo` del frontend, sin cambiar logica de negocio ni flujos funcionales.

## Decisiones visuales

- Se incorporaron los logos de la Facultad de Quimica y Farmacia y de Tecnologia Farmaceutica en puntos de alta visibilidad y baja saturacion:
  - acceso/login,
  - navegacion principal,
  - dashboard principal,
  - favicon del sistema.
- Se sustituyo la paleta azul original por una paleta verde institucional concentrada en `src/index.css`.
- Se mantuvieron superficies claras, contraste alto y estados de alerta diferenciados para no perder legibilidad operativa.
- Los botones principales, badges, tarjetas y estados hover ahora heredan tonos verdes consistentes mediante tokens reutilizables.

## Alcance

- Solo se modifico frontend.
- No se agregaron nuevas funcionalidades.
- No se altero backend.
