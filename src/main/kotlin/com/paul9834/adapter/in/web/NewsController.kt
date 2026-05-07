package com.paul9834.adapter.`in`.web

import com.paul9834.adapter.`in`.web.dto.ArticleResponse
import com.paul9834.adapter.`in`.web.dto.NewsPageResponse
import com.paul9834.domain.port.`in`.NewsUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/news")
@CrossOrigin(origins = ["http://localhost:4200", "https://www.paul9834.com"])
class NewsController(
    private val newsUseCase: NewsUseCase
) {

    @GetMapping
    fun getTopNews(
        @RequestParam(defaultValue = "technology") topic: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "12") size: Int
    ): ResponseEntity<NewsPageResponse> {
        val articles = newsUseCase.getTopNews(topic, page, size)
            .map { NewsMapper.toResponse(it) }

        return ResponseEntity.ok(
            NewsPageResponse(articles = articles, page = page, size = size, topic = topic)
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
}
