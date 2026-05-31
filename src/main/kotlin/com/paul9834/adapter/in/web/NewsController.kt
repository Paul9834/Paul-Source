package com.paul9834.adapter.`in`.web

import com.paul9834.adapter.`in`.web.dto.ArticleRequest
import com.paul9834.adapter.`in`.web.dto.ArticleResponse
import com.paul9834.adapter.`in`.web.dto.NewsPageResponse
import com.paul9834.domain.port.`in`.NewsUseCase
import com.paul9834.domain.port.out.ImageStoragePort
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/news")
@CrossOrigin(origins = ["http://localhost:4200", "https://www.paul9834.com"])
@Tag(name = "News", description = "📰 Endpoints públicos y administrativos para artículos del blog")
class NewsController(
    private val newsUseCase: NewsUseCase,
    private val imageStoragePort: ImageStoragePort
) {

    @Operation(
        summary = "Listar artículos públicos",
        description = "Retorna artículos publicados con paginación y filtro opcional por categoría."
    )
    @ApiResponse(responseCode = "200", description = "Listado público obtenido")
    @GetMapping
    fun getArticles(
        @Parameter(description = "Categoría del artículo", example = "tech")
        @RequestParam(required = false) category: String?,
        @Parameter(description = "Página a consultar", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Cantidad de elementos por página", example = "12")
        @RequestParam(defaultValue = "12") size: Int
    ): ResponseEntity<NewsPageResponse> {
        val articles = newsUseCase.getArticles(category, page, size)
            .map { NewsMapper.toResponse(it) }

        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(
                NewsPageResponse(
                    articles = articles,
                    page = page,
                    size = size,
                    topic = category ?: "all"
                )
            )
    }

    @Operation(
        summary = "Obtener artículo por slug",
        description = "Busca un artículo público por su slug único."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Artículo encontrado"),
            ApiResponse(responseCode = "404", description = "Artículo no encontrado")
        ]
    )
    @GetMapping("/{slug}")
    fun getArticleBySlug(
        @Parameter(description = "Slug único del artículo", example = "spring-boot-kotlin-api")
        @PathVariable slug: String
    ): ResponseEntity<ArticleResponse> {
        val article = newsUseCase.getArticleBySlug(slug)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(NewsMapper.toResponse(article))
    }

    @Operation(
        summary = "Crear artículo",
        description = "Crea un nuevo artículo. Acepta metadata del artículo y una imagen opcional en multipart/form-data.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Artículo creado"),
            ApiResponse(responseCode = "400", description = "Payload inválido"),
            ApiResponse(responseCode = "401", description = "No autenticado")
        ]
    )
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createArticle(
        @Parameter(
            description = "JSON del artículo",
            required = true,
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = ArticleRequest::class),
                examples = [ExampleObject(value = "{\n  \"title\": \"Nueva noticia\",\n  \"description\": \"Resumen breve\",\n  \"content\": \"<p>Contenido</p>\",\n  \"category\": \"tech\",\n  \"published\": false\n}")]
            )]
        )
        @RequestPart("article") request: ArticleRequest,
        @Parameter(description = "Imagen opcional para portada", content = [Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)])
        @RequestPart("image", required = false) image: MultipartFile?
    ): ResponseEntity<ArticleResponse> {
        val imageUrl = image?.let { imageStoragePort.uploadNewsImage(it) }

        val created = newsUseCase.createArticle(
            NewsMapper.toDomain(request, imageUrl = imageUrl)
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(NewsMapper.toResponse(created))
    }

    @Operation(
        summary = "Actualizar artículo",
        description = "Actualiza un artículo existente por slug. Permite reemplazar imagen y contenido.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Artículo actualizado"),
            ApiResponse(responseCode = "401", description = "No autenticado"),
            ApiResponse(responseCode = "404", description = "Artículo no encontrado")
        ]
    )
    @PutMapping("/{slug}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updateArticle(
        @Parameter(description = "Slug del artículo a actualizar", example = "spring-boot-kotlin-api")
        @PathVariable slug: String,
        @RequestPart("article") request: ArticleRequest,
        @RequestPart("image", required = false) image: MultipartFile?
    ): ResponseEntity<ArticleResponse> {
        val imageUrl = image?.let { imageStoragePort.uploadNewsImage(it) }

        val updated = newsUseCase.updateArticle(
            slug,
            NewsMapper.toDomain(request, slug, imageUrl)
        )

        return ResponseEntity.ok(NewsMapper.toResponse(updated))
    }

    @Operation(
        summary = "Eliminar artículo",
        description = "Elimina un artículo por slug.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Artículo eliminado"),
            ApiResponse(responseCode = "401", description = "No autenticado"),
            ApiResponse(responseCode = "404", description = "Artículo no encontrado")
        ]
    )
    @DeleteMapping("/{slug}")
    fun deleteArticle(
        @Parameter(description = "Slug del artículo a eliminar", example = "spring-boot-kotlin-api")
        @PathVariable slug: String
    ): ResponseEntity<Void> {
        newsUseCase.deleteArticle(slug)
        return ResponseEntity.noContent().build()
    }

    @Operation(
        summary = "Publicar artículo",
        description = "Marca un artículo como publicado y fija su fecha de publicación si aún no existe.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @PatchMapping("/{slug}/publish")
    fun publishArticle(
        @Parameter(description = "Slug del artículo a publicar", example = "spring-boot-kotlin-api")
        @PathVariable slug: String
    ): ResponseEntity<ArticleResponse> {
        val published = newsUseCase.publishArticle(slug)
        return ResponseEntity.ok(NewsMapper.toResponse(published))
    }

    @Operation(
        summary = "Dar like a un artículo",
        description = "Incrementa en uno el contador de likes del artículo. Endpoint público."
    )
    @PatchMapping("/{slug}/like")
    fun likeArticle(
        @Parameter(description = "Slug del artículo al que se le dará like", example = "spring-boot-kotlin-api")
        @PathVariable slug: String
    ): ResponseEntity<ArticleResponse> {
        val updated = newsUseCase.likeArticle(slug)
        return ResponseEntity.ok(NewsMapper.toResponse(updated))
    }

    @Operation(
        summary = "Listar artículos administrativos",
        description = "Retorna artículos publicados y borradores para el panel administrativo.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Listado administrativo obtenido"),
            ApiResponse(responseCode = "401", description = "No autenticado"),
            ApiResponse(responseCode = "403", description = "Sin permisos")
        ]
    )
    @GetMapping("/admin")
    fun getAdminArticles(
        @Parameter(description = "Categoría del artículo", example = "tech")
        @RequestParam(required = false) category: String?,
        @Parameter(description = "Página a consultar", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Cantidad de elementos por página", example = "20")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<NewsPageResponse> {
        val articles = newsUseCase.getAdminArticles(category, page, size)
            .map { NewsMapper.toResponse(it) }

        return ResponseEntity.ok(
            NewsPageResponse(
                articles = articles,
                page = page,
                size = size,
                topic = category ?: "all"
            )
        )
    }
}
