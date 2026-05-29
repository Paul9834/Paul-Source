package com.paul9834.adapter.`in`.web

import com.paul9834.adapter.`in`.web.dto.ArticleRequest
import com.paul9834.adapter.`in`.web.dto.ArticleResponse
import com.paul9834.domain.model.Article
import java.text.Normalizer
import java.util.Locale

object NewsMapper {

    fun toResponse(article: Article): ArticleResponse {
        return ArticleResponse(
            slug = article.slug,
            title = article.title,
            description = article.description,
            content = article.content,
            imageUrl = article.imageUrl,
            category = article.category,
            published = article.published,
            publishedAt = article.publishedAt,
            likesCount = article.likesCount
        )
    }

    fun toDomain(
        request: ArticleRequest,
        slug: String? = null,
        imageUrl: String? = null
    ): Article {
        val articleSlug = slug ?: request.title.toSlug()

        return Article(
            slug = articleSlug,
            title = request.title,
            description = request.description,
            content = request.content,
            imageUrl = imageUrl ?: request.imageUrl,
            category = request.category,
            published = request.published,
            publishedAt = if (request.published) {
                java.time.OffsetDateTime.now(
                    java.time.ZoneId.of("America/Bogota")
                ).toString()
            } else {
                ""
            },
            createdAt = "",
            likesCount = 0
        )
    }

    private fun String.toSlug(): String {
        val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
            .lowercase(Locale.getDefault())
            .replace("[^a-z0-9]+".toRegex(), "-")
            .trim('-')
            .take(95)
            .trim('-')

        return normalized.ifBlank { "article" }
    }

}
