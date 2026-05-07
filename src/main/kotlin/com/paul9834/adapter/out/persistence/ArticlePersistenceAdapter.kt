package com.paul9834.adapter.out.persistence

import com.paul9834.paul9834.domain.model.Article
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component
class ArticlePersistenceAdapter(
    private val jpaRepository: ArticleJpaRepository
) {

    fun fetchTopNews(topic: String, maxResults: Int): List<Article> {
        val pageable = PageRequest.of(0, maxResults)
        return jpaRepository
            .findAllByOrderByPublishedAtDesc(pageable)
            .content
            .map { it.toDomain() }
    }

    fun findBySlug(slug: String): Article? {
        return jpaRepository.findBySlug(slug)?.toDomain()
    }

    fun save(article: Article) {
        if (!jpaRepository.existsBySlug(article.slug)) {
            jpaRepository.save(article.toEntity())
        }
    }

    fun saveAll(articles: List<Article>) {
        val newArticles = articles
            .filter { !jpaRepository.existsBySlug(it.slug) }
            .map { it.toEntity() }
        jpaRepository.saveAll(newArticles)
    }
}

private fun ArticleEntity.toDomain() = Article(
    slug = slug,
    title = title,
    description = description,
    content = content,
    url = url,
    imageUrl = imageUrl,
    publishedAt = publishedAt,
    sourceName = sourceName,
    sourceUrl = sourceUrl
)

private fun Article.toEntity() = ArticleEntity(
    slug = slug,
    title = title,
    description = description,
    content = content,
    url = url,
    imageUrl = imageUrl,
    publishedAt = publishedAt,
    sourceName = sourceName,
    sourceUrl = sourceUrl
)
