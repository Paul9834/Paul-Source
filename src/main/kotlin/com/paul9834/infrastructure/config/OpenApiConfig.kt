package com.paul9834.infrastructure.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.info.License
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.servers.Server
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "Paul-Source API 🚀",
        version = "1.0.0",
        description = """
API REST del backend de Paul-Source para autenticación administrativa, gestión editorial de noticias y publicación de contenido del portfolio.

### Highlights
- 🔐 Autenticación JWT para administración
- 📰 CRUD de noticias con flujo público y admin
- 🖼️ Subida de imágenes a Cloudinary
- ⚡ Cache en memoria con Caffeine
- 🧱 Arquitectura hexagonal con Kotlin + Spring Boot
        """,
        contact = Contact(
            name = "Kevin Paul Montealegre Melo",
            url = "https://paul9834.com",
            email = "contacto@paul9834.com"
        ),
        license = License(
            name = "MIT",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = [
        Server(url = "http://localhost:8080", description = "Local 💻"),
        Server(url = "https://api.paul9834.com", description = "Producción 🌐")
    ],
    security = [SecurityRequirement(name = "bearerAuth")]
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    `in` = SecuritySchemeIn.HEADER
)
class OpenApiConfig
