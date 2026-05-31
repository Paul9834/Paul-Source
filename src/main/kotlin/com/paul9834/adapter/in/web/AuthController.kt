package com.paul9834.adapter.`in`.web

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64
import java.util.Date

data class LoginRequest(val password: String)
data class TokenResponse(val token: String)

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = ["http://localhost:4200", "https://www.paul9834.com"])
@Tag(name = "Auth", description = "🔐 Autenticación administrativa y emisión de JWT")
class AuthController {

    @Value("\${app.security.admin.password}")
    private lateinit var adminPassword: String

    @Value("\${app.security.jwt.secret}")
    private lateinit var jwtSecret: String

    @Operation(
        summary = "Generar token JWT de administrador",
        description = "Valida la contraseña administrativa y retorna un JWT bearer para consumir endpoints protegidos del panel editorial."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Token generado correctamente",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TokenResponse::class),
                    examples = [ExampleObject(value = "{\n  \"token\": \"eyJhbGciOiJIUzI1NiJ9...\"\n}")]
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Contraseña inválida"
            )
        ]
    )
    @PostMapping("/token")
    fun getToken(@RequestBody request: LoginRequest): ResponseEntity<TokenResponse> {
        if (request.password != adminPassword) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val now = Instant.now()
        val key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret))

        val token = Jwts.builder()
            .subject("admin")
            .issuer("paul9834")
            .claim("roles", listOf("ADMIN"))
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(365, ChronoUnit.DAYS)))
            .signWith(key)
            .compact()

        return ResponseEntity.ok(TokenResponse(token))
    }
}
