package com.paul9834.adapter.out.persistence

import com.paul9834.domain.model.Article
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class ArticlePersistenceAdapter(
    private val jpaRepository: ArticleJpaRepository
) {

    fun findAdminArticles(category: String?, page: Int, size: Int): List<Article> {
        val pageable = PageRequest.of(page, size)
        val articles = if (category.isNullOrBlank()) {
            jpaRepository.findAllByOrderByCreatedAtDesc(pageable)
        } else {
            jpaRepository.findByCategoryOrderByCreatedAtDesc(category, pageable)
        }

        return articles.content.map { it.toDomain() }
    }


    fun findArticles(category: String?, page: Int, size: Int): List<Article> {
        val pageable = PageRequest.of(page, size)
        val articles = if (category.isNullOrBlank()) {
            jpaRepository.findByPublishedTrueOrderByPublishedAtDesc(pageable)
        } else {
            jpaRepository.findByPublishedTrueAndCategoryOrderByPublishedAtDesc(category, pageable)
        }

        return articles.content.map { it.toDomain() }
    }

    fun findBySlug(slug: String): Article? {
        return jpaRepository.findBySlug(slug)?.toDomain()
    }

    fun save(article: Article): Article {
        if (jpaRepository.existsBySlug(article.slug)) {
            error("Article already exists: ${article.slug}")
        }

        return jpaRepository.save(article.toEntity()).toDomain()
    }

    fun update(slug: String, article: Article): Article {
        val current = jpaRepository.findBySlug(slug) ?: error("Article not found: $slug")

        return jpaRepository.save(article.toEntity(current.id)).toDomain()
    }

    @Transactional
    fun deleteBySlug(slug: String) {
        if (!jpaRepository.existsBySlug(slug)) {
            error("Article not found: $slug")
        }

        jpaRepository.deleteBySlug(slug)
    }

    fun publish(slug: String): Article {
        val current = jpaRepository.findBySlug(slug) ?: error("Article not found: $slug")
        val publishedAt = current.publishedAt.ifBlank { LocalDateTime.now().toString() }
        val published = current.toDomain().copy(published = true, publishedAt = publishedAt)

        return jpaRepository.save(published.toEntity(current.id)).toDomain()
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
    imageUrl = imageUrl,
    publishedAt = publishedAt,
    category = category,
    published = published,
    createdAt = createdAt.toString()
)

private fun Article.toEntity(id: Long = 0) = ArticleEntity(
    id = id,
    slug = slug,
    title = title,
    description = description,
    content = content,
    imageUrl = imageUrl,
    publishedAt = publishedAt,
    category = category,
    published = published,
    createdAt = createdAt.toLocalDateTime()
)

private fun String.toLocalDateTime(): LocalDateTime {
    return if (isBlank()) LocalDateTime.now() else LocalDateTime.parse(this)
}
