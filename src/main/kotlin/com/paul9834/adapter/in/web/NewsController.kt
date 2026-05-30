package com.paul9834.adapter.`in`.web

import com.paul9834.adapter.`in`.web.dto.ArticleRequest
import com.paul9834.adapter.`in`.web.dto.ArticleResponse
import com.paul9834.adapter.`in`.web.dto.NewsPageResponse
import com.paul9834.domain.port.`in`.NewsUseCase
import com.paul9834.domain.port.out.ImageStoragePort
import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/news")
@CrossOrigin(origins = ["http://localhost:4200", "https://www.paul9834.com"])
class NewsController(
    private val newsUseCase: NewsUseCase,
    private val imageStoragePort: ImageStoragePort
) {


    @GetMapping
    fun getArticles(
        @RequestParam(required = false) category: String?,
        @RequestParam(defaultValue = "0") page: Int,
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


    @GetMapping("/{slug}")
    fun getArticleBySlug(
        @PathVariable slug: String
    ): ResponseEntity<ArticleResponse> {
        val article = newsUseCase.getArticleBySlug(slug)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(NewsMapper.toResponse(article))
    }

    @PostMapping(consumes = ["multipart/form-data"])
    fun createArticle(
        @RequestPart("article") request: ArticleRequest,
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

    @PutMapping("/{slug}", consumes = ["multipart/form-data"])
    fun updateArticle(
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

    @DeleteMapping("/{slug}")
    fun deleteArticle(
        @PathVariable slug: String
    ): ResponseEntity<Void> {
        newsUseCase.deleteArticle(slug)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{slug}/publish")
    fun publishArticle(
        @PathVariable slug: String
    ): ResponseEntity<ArticleResponse> {
        val published = newsUseCase.publishArticle(slug)
        return ResponseEntity.ok(NewsMapper.toResponse(published))
    }

    @PatchMapping("/{slug}/like")
    fun likeArticle(
        @PathVariable slug: String
    ): ResponseEntity<ArticleResponse> {
        val updated = newsUseCase.likeArticle(slug)
        return ResponseEntity.ok(NewsMapper.toResponse(updated))
    }


    @GetMapping("/admin")
    fun getAdminArticles(
        @RequestParam(required = false) category: String?,
        @RequestParam(defaultValue = "0") page: Int,
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
