# Demo Checklist

Checklist corta para validar la primera demo funcional de `ues-qyf-inventory-system` en la rama `demo`.

## Credenciales demo

Si el backend se levanta con `APP_DEMO_SEED_ENABLED=true`, el repositorio documenta este usuario base:

- Usuario: `admin`
- Clave: `Admin123*`

Referencia: [backend/inventory/README.md](../backend/inventory/README.md)

## Pantallas que deben funcionar

- `Login` en `/`
- `Dashboard` en `/dashboard`
- `Productos` en `/products`
- `Crear producto` en `/products/new`
- `Niveles de stock` en `/inventory`
- `Registrar entrada` en `/inventory/entries/new`
- `Movimientos recientes` en `/movements`

## Endpoints principales

- `POST /api/auth/login`
- `GET /api/dashboard/summary`
- `GET /api/products`
- `POST /api/products`
- `GET /api/laboratories`
- `GET /api/inventory-stock`
- `POST /api/inventory-movements`
- `GET /api/inventory-movements`

## Flujo manual de demo

1. Iniciar backend y frontend con la base de datos disponible.
2. Confirmar que el login acepta el usuario demo y redirige a `/dashboard`.
3. Verificar en dashboard:
   - tarjetas KPI visibles
   - accesos rapidos hacia productos, nueva entrada, stock y movimientos
   - manejo correcto si no hay datos o si alguna seccion viene vacia
4. Abrir `Productos` y comprobar:
   - carga inicial sin errores
   - estado vacio claro si no hay registros
   - filtros funcionando si ya existen productos
5. Entrar a `Nuevo producto` y crear un producto valido.
6. Confirmar el regreso a `Productos` con mensaje de exito.
7. Abrir `Niveles de stock` y validar:
   - carga inicial
   - resumen superior
   - estado vacio entendible si aun no hay stock
8. Entrar a `Nueva entrada` y registrar inventario por lote para un producto existente.
9. Confirmar el regreso a `Niveles de stock` con mensaje de exito.
10. Revisar `Movimientos` y validar que aparezca la entrada registrada.
11. Desde dashboard o menu lateral, confirmar que la navegacion llega a la pantalla correcta.
12. Probar `Cerrar sesion` y verificar retorno a `/`.
13. Probar acceso a una ruta protegida sin token o con token expirado y confirmar retorno al login.

## Resultado esperado

- El frontend no debe romperse si la API responde listas vacias.
- Los mensajes de error visibles para usuario deben quedar en espanol.
- El dashboard debe seguir renderizando aunque falten propiedades opcionales o vengan en `null`.
