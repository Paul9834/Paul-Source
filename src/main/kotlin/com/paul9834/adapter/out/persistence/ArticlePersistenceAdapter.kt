package com.paul9834.adapter.out.persistence

import com.paul9834.domain.model.Article
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class ArticlePersistenceAdapter(
    private val jpaRepository: ArticleJpaRepository
) {

    private val colombiaZone: ZoneId = ZoneId.of("America/Bogota")

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
            jpaRepository.findByPublishedTrueOrderByCreatedAtDesc(pageable)
        } else {
            jpaRepository.findByPublishedTrueAndCategoryOrderByCreatedAtDesc(category, pageable)
        }

        return articles.content.map { it.toDomain() }
    }

    fun findBySlug(slug: String): Article? {
        return jpaRepository.findBySlug(slug)?.toDomain()
    }

    fun save(article: Article): Article {
        require(article.slug.length <= 100) { "Slug too long: ${article.slug.length}" }
        require(article.category.length <= 100) { "Category too long: ${article.category.length}" }

        if (jpaRepository.existsBySlug(article.slug)) {
            throw IllegalArgumentException("Article already exists: ${article.slug}")
        }

        return jpaRepository.save(article.toEntity()).toDomain()
    }

    fun update(slug: String, article: Article): Article {
        val current = jpaRepository.findBySlug(slug) ?: error("Article not found: $slug")

        val updatedArticle = article.copy(
            createdAt = current.createdAt.toString(),
            publishedAt = if (article.published) {
                article.publishedAt.ifBlank { current.publishedAt.ifBlank { LocalDateTime.now(colombiaZone).toString() } }
            } else {
                ""
            },
            likesCount = article.likesCount
        )

        return jpaRepository.save(updatedArticle.toEntity(current.id)).toDomain()
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
        val publishedAt = current.publishedAt.ifBlank { LocalDateTime.now(colombiaZone).toString() }
        val published = current.toDomain().copy(
            published = true,
            publishedAt = publishedAt,
            createdAt = current.createdAt.toString()
        )

        return jpaRepository.save(published.toEntity(current.id)).toDomain()
    }

    fun saveAll(articles: List<Article>) {
        val newArticles = articles
            .filter { !jpaRepository.existsBySlug(it.slug) }
            .map { article ->
                val normalized = article.copy(
                    createdAt = article.createdAt.ifBlank { LocalDateTime.now(colombiaZone).toString() },
                    publishedAt = if (article.published) {
                        article.publishedAt.ifBlank { LocalDateTime.now(colombiaZone).toString() }
                    } else {
                        ""
                    }
                )
                normalized.toEntity()
            }

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
    createdAt = createdAt.toString(),
    likesCount = likesCount
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
    createdAt = createdAt.toLocalDateTime(),
    likesCount = likesCount
)

private fun String.toLocalDateTime(): LocalDateTime {
    val colombiaZone = ZoneId.of("America/Bogota")
    return if (isBlank()) LocalDateTime.now(colombiaZone) else LocalDateTime.parse(this)
}
