# Paul-Source — Spring Boot Backend API

Backend del portfolio profesional de Kevin Paul Montealegre Melo, construido con **Spring Boot 3.4.5**, **Kotlin 2.0** y **Java 21**. Expone una API REST segura para gestión editorial del blog, autenticación basada en JWT y almacenamiento de imágenes en Cloudinary. Diseñado para operar en producción sobre un VPS con MySQL, ejecutándose como servicio systemd detrás de Nginx.

## Descripción general

El proyecto es el backend del portfolio `paul9834.com` y cumple dos responsabilidades principales: servir contenido público de noticias/blog al frontend Angular y proteger operaciones administrativas mediante autenticación JWT. La aplicación sigue una arquitectura hexagonal estricta donde el dominio no depende de ninguna tecnología de infraestructura, y los adaptadores de entrada y salida se intercambian libremente.

La build genera un JAR ejecutable con `spring-boot-maven-plugin`, que el sistema operativo gestiona mediante una unidad systemd incluida en el repositorio (`paul-source.service`). Toda configuración sensible se lee desde variables de entorno en tiempo de ejecución, manteniando el código limpio de secretos.

## Stack técnico

| Capa | Tecnología | Propósito |
|---|---|---|
| Lenguaje | Kotlin 2.0 + Java 21 | Tipado seguro, null-safety nativa y coroutines ready. |
| Framework | Spring Boot 3.4.5 | Autoconfiguración, IoC, MVC REST y lifecycle de aplicación. |
| Seguridad | Spring Security + OAuth2 Resource Server + JJWT 0.12.6 | Autenticación stateless con JWT firmado con HMAC-SHA256. |
| Persistencia | Spring Data JPA + Hibernate + MySQL | ORM con DDL automático y queries derivadas por convención. |
| Caché | Spring Cache + Caffeine | Cache en memoria por clave compuesta para reads frecuentes. |
| Storage | Cloudinary SDK (`cloudinary-http44`) | Subida y gestión de imágenes con URL segura HTTPS. |
| HTTP reactivo | Spring WebFlux (`WebClient`) | Consumo reactivo de APIs externas cuando se requiera. |
| Build | Maven + kotlin-maven-plugin | Compilación, empaquetado y deploy del JAR. |
| Despliegue | systemd + VPS | Ejecución persistente, reinicio automático y logs via journal. |

## Arquitectura del proyecto

El proyecto implementa **Arquitectura Hexagonal (Ports & Adapters)** con tres capas bien diferenciadas que respetan la regla de dependencia hacia adentro.

### Capa de Dominio (`domain/`)

Contiene el modelo de negocio puro, sin imports de Spring, JPA ni ninguna biblioteca externa. `Article` es una `data class` inmutable con todos los campos del contenido editorial. `NewsUseCase` e `ImageStoragePort` son interfaces que definen contratos de entrada y salida respectivamente, garantizando que el dominio solo conoce sus propias abstracciones.

### Capa de Aplicación (`application/`)

`NewsService` implementa `NewsUseCase` y orquesta la lógica de negocio apoyándose en `ArticlePersistenceAdapter`. Cada operación de lectura está anotada con `@Cacheable` usando claves compuestas por categoría, página y tamaño; cada mutación dispara `@CacheEvict` sobre los tres caches (`news`, `admin-news`, `article`), manteniendo consistencia sin invalidación manual. La operación `likeArticle` realiza un ciclo read-modify-write atómico en la capa de servicio.

### Capa de Adaptadores (`adapter/`)

Separada en dos subdirectorios que reflejan la dirección del flujo:

- **`adapter/in/web/`** — controladores REST que reciben requests HTTP, delegan en el use case y devuelven DTOs serializados. Incluye mapper, DTOs y `GlobalExceptionHandler`.
- **`adapter/out/persistence/`** — traduce entre `Article` (dominio) y `ArticleEntity` (JPA), ejecuta queries paginadas y gestiona transacciones.
- **`adapter/out/cloudinary/`** — implementa `ImageStoragePort`, valida el archivo y lo sube a Cloudinary bajo el prefijo `paul9834/news`.

## Patrones identificados

### 1. Hexagonal Architecture (Ports & Adapters)

El dominio define interfaces (`NewsUseCase`, `ImageStoragePort`) que los adaptadores implementan. `NewsService` nunca importa `ArticleJpaRepository` ni Cloudinary directamente; solo conoce `ArticlePersistenceAdapter` como componente Spring, lo que preserva la separación. Este patrón facilita sustitución de infraestructura sin modificar la lógica de negocio.

### 2. Stateless JWT con Spring Security OAuth2 Resource Server

En lugar de una implementación manual de filtros, el proyecto configura Spring como Resource Server OAuth2, delegando la validación de tokens al `JwtDecoder` de Nimbus configurado con clave HMAC-SHA256. `JwtConfig` construye encoder y decoder a partir de un secreto en Base64, y `JwtAuthenticationConverter` mapea el claim `roles` del token a `GrantedAuthority` con prefijo `ROLE_`. Esto permite usar `hasRole("ADMIN")` en las reglas de Spring Security de forma declarativa.

### 3. Generación de JWT en `AuthController`

El controlador de autenticación usa JJWT (`io.jsonwebtoken`) para firmar el token en el endpoint `POST /api/auth/token`. Valida la contraseña contra la variable de entorno `ADMIN_PASSWORD`, y si es correcta emite un JWT con claim `roles: ["ADMIN"]`, subject `admin` y expiración configurable por `JWT_EXPIRATION_HOURS` (por defecto 8760h = 1 año). Esta combinación de JJWT para firma y Nimbus para validación es deliberada: separa las responsabilidades de emisión y verificación.

### 4. Caché declarativa con Caffeine

`@EnableCaching` en `Main.kt` activa el motor de caché, y las dependencias `spring-boot-starter-cache` + `caffeine` lo implementan en memoria. Los métodos de lectura en `NewsService` usan claves compuestas dinámicas (`#category + '_' + #page + '_' + #size`) para granularidad por página y categoría. Toda mutación invalida los tres caches simultáneamente con `allEntries = true`, garantizando que el frontend nunca recibe datos obsoletos.

### 5. Mapeo dominio-entidad en adaptador de persistencia

`ArticlePersistenceAdapter` contiene funciones de extensión privadas `ArticleEntity.toDomain()` y `Article.toEntity(id)` que realizan la traducción bidireccional. El adaptador también centraliza la lógica de zona horaria (`America/Bogota`) para `createdAt` y `publishedAt`, evitando que este detalle de infraestructura contamine el dominio.

### 6. Slug auto-generado con normalización Unicode

`NewsMapper.toSlug()` normaliza el título usando `Normalizer.Form.NFD`, elimina diacríticos, convierte a minúsculas, reemplaza caracteres no alfanuméricos por guiones y limita a 95 caracteres. Esto permite crear artículos con títulos en español sin preocuparse por la URL resultante, y la clave de slug sirve como identificador público único en lugar de exponer IDs numéricos.

### 7. CORS estricto por origen

`SecurityConfig.corsConfigurationSource()` define explícitamente los orígenes permitidos (`localhost:4200` para desarrollo, `paul9834.com` y `www.paul9834.com` para producción), métodos y headers. La configuración no usa wildcards, lo que reduce la superficie de ataque y es coherente con un backend que solo sirve a un frontend conocido.

### 8. Manejo centralizado de errores

`GlobalExceptionHandler` con `@RestControllerAdvice` intercepta `IllegalArgumentException` (→ `400 Bad Request`), `IllegalStateException` (→ `409 Conflict`) y `Exception` genérica (→ `500 Internal Server Error`), serializando siempre una respuesta JSON `{"error": "..."}`. Esto desacopla el manejo de errores de los controladores y provee respuestas consistentes para el frontend.

## Seguridad

El modelo de seguridad es stateless por diseño: no hay sesiones HTTP, no hay cookies. Cada request autenticado porta un JWT en el header `Authorization: Bearer <token>`, que el resource server valida localmente sin consultar base de datos.

### Matriz de permisos

| Endpoint | Método | Acceso |
|---|---|---|
| `/api/auth/token` | `POST` | Público |
| `/api/news` | `GET` | Público |
| `/api/news/{slug}` | `GET` | Público |
| `/api/news/{slug}/like` | `PATCH` | Público |
| `/api/news/admin` | `GET` | `ROLE_ADMIN` |
| `/api/news` | `POST` | Autenticado |
| `/api/news/{slug}` | `PUT` | Autenticado |
| `/api/news/{slug}` | `DELETE` | Autenticado |
| `/api/news/{slug}/publish` | `PATCH` | Autenticado |
| `OPTIONS /**` | `OPTIONS` | Público (preflight CORS) |

### Flujo de autenticación

```
Cliente Angular
    │
    ├─ POST /api/auth/token  { password: "..." }
    │         │
    │         ▼
    │   AuthController valida ADMIN_PASSWORD
    │   Firma JWT con JJWT (HMAC-SHA256) → claim roles: ["ADMIN"]
    │         │
    │         ▼
    │   { token: "eyJ..." }
    │
    ├─ GET /api/news/admin   Authorization: Bearer eyJ...
    │         │
    │         ▼
    │   Spring Security → JwtDecoder (Nimbus) valida firma
    │   JwtAuthenticationConverter extrae roles → ROLE_ADMIN
    │   hasRole("ADMIN") → acceso permitido
    │         │
    │         ▼
    │   NewsController → NewsService → DB
```

## Endpoints de la API

### Autenticación

```
POST /api/auth/token
Content-Type: application/json

{ "password": "tu-contraseña-admin" }

→ 200 OK: { "token": "eyJ..." }
→ 401 Unauthorized
```

### Noticias — Consumo público

```
GET  /api/news?category=tech&page=0&size=12
GET  /api/news/{slug}
PATCH /api/news/{slug}/like
```

### Noticias — Panel administrativo (requiere JWT)

```
GET    /api/news/admin?category=tech&page=0&size=20
POST   /api/news          (multipart/form-data: article + image)
PUT    /api/news/{slug}   (multipart/form-data: article + image)
DELETE /api/news/{slug}
PATCH  /api/news/{slug}/publish
```

### Estructura de respuesta `ArticleResponse`

```json
{
  "slug": "mi-articulo-de-tecnologia",
  "title": "Mi Artículo de Tecnología",
  "description": "Descripción breve del artículo.",
  "content": "<p>Contenido completo en HTML...</p>",
  "imageUrl": "https://res.cloudinary.com/...",
  "category": "tech",
  "published": true,
  "publishedAt": "2026-05-31T01:00:00",
  "likesCount": 42
}
```

### Estructura de respuesta paginada `NewsPageResponse`

```json
{
  "articles": [...],
  "page": 0,
  "size": 12,
  "topic": "tech"
}
```

## Almacenamiento de imágenes

La integración con Cloudinary sigue el mismo patrón de port/adapter. `ImageStoragePort` define el contrato; `CloudinaryImageStorageAdapter` implementa la subida con validaciones previas: archivo no vacío, `Content-Type` debe iniciar con `image/` y tamaño máximo de 5 MB (el límite multipart del servidor es 15 MB para dar margen). Las imágenes se almacenan bajo la carpeta `paul9834/news` en Cloudinary y la URL segura HTTPS se persiste en la base de datos.

## Persistencia y modelo de datos

La tabla `articles` en MySQL se gestiona por `spring.jpa.hibernate.ddl-auto=update`, lo que significa que Hibernate crea o actualiza el esquema automáticamente al iniciar la aplicación. No se requiere script DDL manual para el primer despliegue.

### Campos de `ArticleEntity`

| Campo | Tipo | Restricción |
|---|---|---|
| `id` | `BIGINT` | PK, auto-increment |
| `slug` | `VARCHAR(100)` | Único, no nulo |
| `title` | `VARCHAR(500)` | No nulo |
| `description` | `TEXT` | - |
| `content` | `LONGTEXT` | - |
| `imageUrl` | `VARCHAR(1000)` | Nullable |
| `category` | `VARCHAR(100)` | No nulo, default `general` |
| `published` | `BOOLEAN` | No nulo |
| `publishedAt` | `VARCHAR` | Vacío si no publicado |
| `createdAt` | `DATETIME` | Auto, zona Colombia |
| `likesCount` | `BIGINT` | No nulo, default 0 |

## Configuración por variables de entorno

Toda la configuración sensible se externaliza mediante `${VAR}` en `application.properties`. El repositorio incluye `paul-source.env.example` como referencia.

```properties
# application.properties — valores desde entorno
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}

app.security.jwt.secret=${JWT_SECRET}
app.security.jwt.expiration-hours=${JWT_EXPIRATION_HOURS:8760}
app.security.admin.password=${ADMIN_PASSWORD}

cloudinary.cloud-name=${CLOUDINARY_CLOUD_NAME}
cloudinary.api-key=${CLOUDINARY_API_KEY}
cloudinary.api-secret=${CLOUDINARY_API_SECRET}
```

### Variables requeridas

| Variable | Descripción | Ejemplo |
|---|---|---|
| `DB_URL` | JDBC URL completo de MySQL | `jdbc:mysql://localhost:3306/paul_source?useSSL=false&serverTimezone=UTC` |
| `DB_USER` | Usuario de base de datos | `paul_source_user` |
| `DB_PASS` | Contraseña de base de datos | `CHANGE_ME` |
| `JWT_SECRET` | Secreto HMAC-SHA256 en Base64 (mínimo 256 bits) | `CHANGE_ME` |
| `JWT_EXPIRATION_HOURS` | Duración del token en horas (default 8760) | `8760` |
| `ADMIN_PASSWORD` | Contraseña del panel administrativo | `CHANGE_ME` |
| `CLOUDINARY_CLOUD_NAME` | Nombre de cloud en Cloudinary | `dgv49xpsu` |
| `CLOUDINARY_API_KEY` | API Key de Cloudinary | `CHANGE_ME` |
| `CLOUDINARY_API_SECRET` | API Secret de Cloudinary | `CHANGE_ME` |

## Despliegue en VPS

El repositorio incluye `paul-source.service`, una unidad systemd lista para producción que define usuario, directorio de trabajo, archivo de entorno y política de reinicio automático.

### Proceso de despliegue manual

1. Compilar el JAR en local o en CI:

```bash
mvn clean package -DskipTests
```

2. Transferir el artefacto al VPS:

```bash
scp target/paul-source-1.0.0.jar user@vps:/opt/paul-source/app.jar
```

3. Instalar el servicio systemd:

```bash
sudo cp paul-source.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable paul-source
```

4. Crear el archivo de entorno en el servidor:

```bash
sudo cp paul-source.env.example /etc/paul-source.env
sudo nano /etc/paul-source.env   # completar valores reales
sudo chmod 600 /etc/paul-source.env
```

5. Iniciar el servicio:

```bash
sudo systemctl start paul-source
sudo systemctl status paul-source
```

### Gestión del servicio

```bash
# Ver logs en tiempo real
journalctl -u paul-source -f

# Reiniciar tras actualización
sudo systemctl restart paul-source

# Detener
sudo systemctl stop paul-source
```

### Bloque Nginx recomendado

```nginx
server {
    listen 80;
    server_name api.paul9834.com;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

El bloque anterior expone el API en `api.paul9834.com`, que es la URL base configurada en los entornos de producción del frontend Angular. Con Certbot/Let's Encrypt se gestiona el certificado TLS sobre este subdominio.

## Estructura del proyecto

```text
src/
└── main/
    └── kotlin/com/paul9834/
        ├── Main.kt                               ← @SpringBootApplication, @EnableCaching, @EnableScheduling
        ├── domain/
        │   ├── model/
        │   │   └── Article.kt                    ← data class inmutable del dominio
        │   └── port/
        │       ├── in/
        │       │   └── NewsUseCase.kt            ← contrato de entrada (interface)
        │       └── out/
        │           └── ImageStoragePort.kt       ← contrato de salida (interface)
        ├── application/
        │   └── NewsService.kt                    ← lógica de negocio + caché declarativa
        ├── adapter/
        │   ├── in/
        │   │   └── web/
        │   │       ├── AuthController.kt         ← POST /api/auth/token
        │   │       ├── NewsController.kt         ← CRUD /api/news
        │   │       ├── NewsMapper.kt             ← conversión request/response ↔ domain
        │   │       ├── GlobalExceptionHandler.kt ← @RestControllerAdvice
        │   │       └── dto/
        │   │           ├── ArticleRequest.kt
        │   │           ├── ArticleResponse.kt
        │   │           └── NewsPageResponse.kt
        │   └── out/
        │       ├── persistence/
        │       │   ├── ArticleEntity.kt          ← entidad JPA
        │       │   ├── ArticleJpaRepository.kt   ← queries derivadas por convención
        │       │   └── ArticlePersistenceAdapter.kt ← traduce dominio ↔ JPA
        │       └── cloudinary/
        │           └── CloudinaryImageStorageAdapter.kt ← implementa ImageStoragePort
        └── infrastructure/
            └── config/
                ├── SecurityConfig.kt             ← reglas de autorización + CORS
                ├── JwtConfig.kt                  ← JwtEncoder, JwtDecoder, JwtConverter
                └── CloudinaryConfig.kt           ← bean Cloudinary
```

## Ejecución local

1. Requisitos previos: Java 21, Maven 3.9+, MySQL 8+ corriendo localmente.

2. Crear base de datos:

```sql
CREATE DATABASE paul_source;
CREATE USER 'paul_source_user'@'localhost' IDENTIFIED BY 'dev-password';
GRANT ALL PRIVILEGES ON paul_source.* TO 'paul_source_user'@'localhost';
```

3. Copiar y completar variables de entorno:

```bash
cp paul-source.env.example .env
# Editar .env con valores locales
```

4. Exportar variables en la sesión de terminal:

```bash
export $(cat .env | xargs)
```

5. Compilar y ejecutar:

```bash
mvn spring-boot:run
```

La aplicación levanta en `http://localhost:8080`.

## Scripts Maven disponibles

| Comando | Función |
|---|---|
| `mvn spring-boot:run` | Levanta la aplicación en modo desarrollo. |
| `mvn clean package` | Genera el JAR ejecutable en `target/`. |
| `mvn clean package -DskipTests` | Build sin tests, útil en CI rápido. |
| `mvn test` | Ejecuta suite de tests unitarios con JUnit 5. |
| `mvn clean verify` | Build completo incluyendo tests de integración. |

## Calidad técnica

La solución aplica decisiones maduras para un backend de portfolio productivo: arquitectura hexagonal para separación de responsabilidades, JWT stateless para no requerir estado de sesión en servidor, caché en memoria con invalidación granular para reducir carga en base de datos, almacenamiento de imágenes externalizado en Cloudinary para no cargar el disco del VPS, y operación supervisada por systemd con reinicio automático. La combinación de Kotlin, Spring Boot 3 y Java 21 representa el stack de backend moderno con soporte de largo plazo y alta compatibilidad con el ecosistema JVM.

## Roadmap técnico sugerido

- Añadir tests de integración con `@SpringBootTest` y base de datos H2 en memoria para validar el stack completo sin MySQL.
- Implementar refresh token o expiración más corta del JWT con renovación automática desde el frontend.
- Agregar paginación completa en `NewsPageResponse` (total de elementos y páginas) para que el frontend implemente navegación correcta.
- Documentar la API con OpenAPI/Swagger usando `springdoc-openapi-starter-webmvc-ui`.
- Considerar migración de `ddl-auto=update` a Liquibase o Flyway para control de esquema en producción.
- Añadir observabilidad con Spring Actuator + métricas exportadas a Prometheus/Grafana.
