package com.paul9834.adapter.`in`.web

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64
import java.util.Date

data class LoginRequest(val password: String)
data class TokenResponse(val token: String)

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = ["http://localhost:4200", "https://www.paul9834.com"])
class AuthController {

    @Value("\${app.security.admin.password}")
    private lateinit var adminPassword: String

    @Value("\${app.security.jwt.secret}")
    private lateinit var jwtSecret: String

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
