# Despliegue de demo temporal

Este flujo deja el proyecto listo para pruebas funcionales basicas con:

- `Frontend`: Vercel
- `Backend`: Render Web Service con Docker
- `Base de datos`: Render Postgres Free

## 1. Base de datos en Render

La demo temporal puede usar `Render Postgres Free` directamente. Para este caso es la opcion mas simple porque el backend y la base quedarian en la misma plataforma.

Importante:

- Render indica que una base `Free Render Postgres` expira 30 dias despues de su creacion.
- Solo se permite una base Postgres gratuita activa por workspace.
- Las bases gratuitas no incluyen backups.

La aplicacion ejecuta Flyway al iniciar, por lo que el esquema se crea automaticamente.

## 2. Backend en Render

El backend y la base se pueden crear desde [render.yaml](C:\Users\Juan.Rivera\Documents\HorasSociales\ues-qyf-inventory-system\render.yaml) usando un Blueprint.

Variables importantes:

- `APP_CORS_ALLOWED_ORIGINS`
- `APP_DEFAULT_ADMIN_PASSWORD`
- `JWT_SECRET`

Variables que ya quedan definidas para la demo:

- `APP_DEMO_SEED_ENABLED=true`
- `APP_DEFAULT_ADMIN_ENABLED=true`
- `APP_DEFAULT_ADMIN_USERNAME=admin`
- `APP_DEFAULT_ADMIN_EMAIL=admin@qyf.demo`
- `APP_DEFAULT_ADMIN_FULL_NAME=Administrador Demo`

Orden recomendado:

1. En Render, crear un nuevo Blueprint apuntando al repositorio y rama `demo`.
2. Confirmar la creacion de la base `qyf-inventory-db-demo` y del servicio `qyf-inventory-api-demo`.
3. Completar las variables marcadas como secretas.
4. Esperar el primer deploy.
5. Verificar `https://tu-backend.onrender.com/api/health`.

El `render.yaml` ya enlaza automaticamente:

- `DB_URL` desde `connectionString`
- `DB_USERNAME` desde `user`
- `DB_PASSWORD` desde `password`

El backend normaliza automaticamente el `connectionString` de Render (`postgresql://user:password@host:port/database`) al formato JDBC requerido por Spring Boot (`jdbc:postgresql://host:port/database`).

## 3. Frontend en Vercel

Configura el proyecto de Vercel usando como raiz `frontend`.

Configuracion esperada:

- Build command: `npm run build`
- Output directory: `dist`
- Install command: `npm install`

Variable obligatoria:

- `VITE_API_URL=https://tu-backend.onrender.com/api`

El archivo [frontend/vercel.json](C:\Users\Juan.Rivera\Documents\HorasSociales\ues-qyf-inventory-system\frontend\vercel.json) ya agrega el rewrite necesario para `BrowserRouter`.

## 4. Credenciales de demo

El usuario administrador demo ya no tiene password fija en el codigo.

Debes definirla en Render con:

- `APP_DEFAULT_ADMIN_PASSWORD`

Sugerencia practica:

- Usa una clave temporal fuerte y compartela solo con quienes probaran el sistema.
- Cuando termine la demo, elimina el servicio o rota la clave.

## 5. Flujo de validacion

Despues de desplegar:

1. Entrar al frontend.
2. Iniciar sesion con el admin demo.
3. Validar dashboard, productos, inventario y movimientos.
4. Confirmar que el seed creo datos de ejemplo.
5. Revisar Swagger en `https://tu-backend.onrender.com/swagger-ui.html`.

## 6. Limitaciones de esta demo

- El backend gratuito de Render puede entrar en reposo por inactividad.
- La base gratuita de Render expira 30 dias despues de creada.
- Los documentos del modulo de adjuntos guardan rutas, no archivos persistidos en almacenamiento administrado.
- Esta configuracion esta pensada para demostracion y pruebas basicas, no para produccion.
