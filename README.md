📦 Sistema de Inventario QyF

Sistema de inventario desarrollado para la Facultad de Química y Farmacia – Universidad de El Salvador, orientado al control de insumos, reactivos y materiales por laboratorio, con trazabilidad completa de movimientos, lotes y vencimientos.

🚀 Tecnologías utilizadas
Backend
* Java 17+
* Spring Boot
* Spring Security (JWT)
* Spring Data JPA
* Flyway (migraciones)
* PostgreSQL
* OpenAPI / Swagger
* Testing
* JUnit 5
* Testcontainers (PostgreSQL real)
  
🧩 Funcionalidades principales
* 🔐 Autenticación y autorización con JWT
* 👥 Gestión de usuarios y roles (ADMIN, ENCARGADO, ESTUDIANTE)
* 🧪 Gestión de productos con unidades de medida (masa, volumen, conteo)
* 🏢 Control de inventario por laboratorio
* 📦 Manejo de lotes con fechas de vencimiento
* 🔄 Registro de movimientos (entrada, salida, ajuste)
* 📊 Trazabilidad completa por usuario, fecha y lote
* ⚠️ Generación de alertas (bajo stock, vencimientos)
* 📎 Soporte para documentos adjuntos (PDF, imágenes)
* 🗑️ Eliminación lógica (soft delete)
* 🧾 Auditoría de cambios (audit_log)
* 🔐 Restricción de acceso por laboratorio
  
🗄️ Base de datos
* Motor: PostgreSQL
* Base de datos: qyf_inventory
* Puerto (Docker): 5433

Migraciones

Gestionadas con Flyway:

* V1 - V5 → esquema completo alineado con el modelo ER actualizado

⚙️ Configuración del entorno
1. Clonar repositorio
git clone https://github.com/jccruzgit/ues-qyf-inventory-system.git
cd ues-qyf-inventory-system

2. Configurar PostgreSQL (Docker recomendado)
docker run -d \
  --name qyf-postgres \
  -e POSTGRES_DB=qyf_inventory \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5433:5432 \
  postgres:15

3. Configurar application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/qyf_inventory
    username: postgres
    password: postgres

  jpa:
    hibernate:
      ddl-auto: validate

  flyway:
    enabled: true
    
4. Ejecutar el proyecto
./mvnw spring-boot:run

O desde IntelliJ → Run Application

📘 Documentación API

Swagger disponible en:

http://localhost:8080/swagger-ui.html

🧪 Pruebas

El proyecto incluye tests de integración con PostgreSQL real usando Testcontainers.

Para ejecutar:

./mvnw test

Valida:

* migraciones Flyway
* persistencia real
* integridad referencial
* relaciones entre entidades

🔐 Seguridad
* Autenticación basada en JWT
* Protección de endpoints
* Control de acceso por:
    * roles
    * laboratorio asignado
      
📂 Estructura del proyecto

backend/
 └── inventory/
     ├── config/
     ├── controller/
     ├── dto/
     ├── entity/
     ├── repository/
     ├── security/
     ├── service/
     └── resources/
         └── db/migration/

📈 Estado del proyecto

✔ Backend funcional
✔ Modelo ER implementado
✔ Migraciones completas
✔ Tests de integración
🔄 En mejora continua (documentación, pruebas funcionales)

👨‍💻 Autor

Juan Carlos Rivera Cruz
Proyecto de Servicio Social
Universidad de El Salvador

📌 Notas

Este proyecto fue desarrollado como parte de un sistema académico, pero siguiendo prácticas utilizadas en sistemas empresariales reales:

* diseño normalizado (3FN)
* trazabilidad completa
* control por laboratorio
* auditoría
* pruebas de integración
