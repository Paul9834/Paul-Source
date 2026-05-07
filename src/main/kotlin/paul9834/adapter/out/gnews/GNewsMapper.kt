package com.paul9834.paul9834.adapter.out.gnews

import com.paul9834.paul9834.domain.model.Article

object GNewsMapper {

    fun toDomain(dto: GNewsArticleDto): Article {
        return Article(
            slug = generateSlug(dto.title),
            title = dto.title,
            description = dto.description ?: "",
            content = dto.content ?: "",
            url = dto.url,
            imageUrl = dto.image,
            publishedAt = dto.publishedAt,
            sourceName = dto.source.name,
            sourceUrl = dto.source.url
        )
    }

    private fun generateSlug(title: String): String {
        return title
            .lowercase()
            .trim()
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .replace(Regex("\\s+"), "-")
            .replace(Regex("-+"), "-")
            .take(80)
    }
}

