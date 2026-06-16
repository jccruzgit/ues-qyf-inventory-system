# Despliegue de demo: opciones y pasos

Fecha de referencia: 15 de junio de 2026.

## Objetivo

Dejar documentada una ruta simple para volver a publicar la demo del sistema aunque no se trabaje siempre desde este equipo.

## Recomendacion principal

Usar esta combinacion:

- Frontend: Vercel Hobby o Cloudflare Pages
- Backend: Render
- Base de datos: Neon Free

## Motivo de esta recomendacion

- El frontend actual es un proyecto Vite/React, por lo que encaja bien en Vercel o Cloudflare Pages.
- Render sigue siendo una opcion razonable para el backend y permite exponer un servicio web por URL.
- Neon ofrece Postgres gratis sin limite de tiempo segun su pricing actual, lo cual evita depender del limite de 30 dias del Postgres free de Render.
- Railway no es la mejor opcion para esta demo si se quiere algo que siga disponible mas alla de un mes, porque su prueba gratis es por creditos temporales.

## Estado verificado de las plataformas

Verificado el 15 de junio de 2026:

- Render muestra plan Hobby de `$0/mes`, `Static Sites` a `$0/mes`, `Web Services` desde `$0/mes` y `Render Postgres Free` con limite de 30 dias.
- Vercel mantiene plan Hobby para proyectos personales.
- Cloudflare Pages indica inicio gratis y menciona sitios, usuarios, requests y bandwidth sin limite en sus planes.
- Neon mantiene plan Free sin limite de tiempo.
- Railway ofrece una prueba gratis de `$5` en creditos con vencimiento de 30 dias.

Fuentes oficiales:

- Render: https://render.com/pricing
- Vercel: https://vercel.com/pricing
- Cloudflare Pages: https://www.cloudflare.com/products/pages/
- Neon: https://neon.com/pricing
- Railway: https://railway.com/pricing

## Opcion recomendada para esta demo

### Opcion A

- Frontend en Vercel
- Backend en Render
- Base de datos en Neon

Es la opcion mas directa si ya se habia trabajado con Vercel y Render.

### Opcion B

- Frontend en Cloudflare Pages
- Backend en Render
- Base de datos en Neon

Conviene si se quiere un frontend estatico muy simple de mantener y con buena distribucion global.

## Variables y datos que deben quedar a mano

Antes de irse del equipo o cambiar de lugar, dejar guardado esto en un gestor de notas seguro:

- URL del repositorio
- Rama que se desea publicar: `demo`
- URL del backend publicado
- URL del frontend publicado
- URL de conexion de Neon
- Usuario y correo usados en Vercel, Render y Neon
- Variables de entorno del frontend
- Variables de entorno del backend

## Variables de entorno minimas del frontend

Confirmar al momento de desplegar:

- `VITE_API_URL`

Ejemplo:

```env
VITE_API_URL=https://tu-backend-demo.onrender.com/api
```

## Flujo recomendado de despliegue

### 1. Publicar la base de datos en Neon

- Crear un proyecto nuevo en Neon.
- Obtener la cadena de conexion de Postgres.
- Guardar la URL para el backend.

### 2. Publicar el backend en Render

- Conectar el repositorio en Render.
- Crear un `Web Service`.
- Seleccionar la rama `demo` o la rama backend que corresponda.
- Configurar variables de entorno del backend, incluyendo la conexion a Neon.
- Esperar que Render genere la URL publica.
- Probar un endpoint simple de salud o autenticacion.

### 3. Publicar el frontend

#### Si se usa Vercel

- Importar el repositorio en Vercel.
- Seleccionar la rama `demo`.
- Configurar `VITE_API_URL` con la URL publica del backend.
- Ejecutar el deploy.

#### Si se usa Cloudflare Pages

- Crear proyecto conectado a GitHub.
- Seleccionar la rama `demo`.
- Comando de build: `npm run build`
- Directorio de salida: `dist`
- Configurar `VITE_API_URL`
- Ejecutar el deploy.

## Checklist rapido antes de compartir la demo

- El login responde correctamente.
- La URL del frontend apunta al backend correcto.
- El backend tiene CORS habilitado para la URL final del frontend.
- La base de datos contiene datos de prueba suficientes.
- Se probaron las rutas criticas:
- `/manufactured-products`
- `/recipes`
- `/production`
- La impresion de formula y descargo funciona.

## Recomendacion operativa

Para no depender de estar fisicamente en este equipo:

- Mantener este archivo actualizado en la rama `demo`.
- Guardar las variables de entorno en un gestor seguro fuera de la maquina local.
- Dejar un respaldo de los datos demo de la base de datos.
- Si se vuelve a publicar la demo, hacerlo siempre desde la rama `demo` para no mezclar cambios en progreso.

## Plan minimo para la proxima vez

Si luego se necesita desplegar rapido:

1. Crear o reactivar base de datos en Neon.
2. Levantar backend en Render con esa conexion.
3. Publicar frontend en Vercel o Cloudflare Pages con `VITE_API_URL`.
4. Ejecutar una prueba funcional corta de login, productos a elaborar, formulas, elaboracion e impresion.

## Nota importante

Los precios, limites y planes de plataformas cambian con frecuencia. Antes de redeployar, volver a revisar las paginas oficiales enlazadas arriba para confirmar que no haya cambios.
