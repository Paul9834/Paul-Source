package com.paul9834.application

import com.paul9834.adapter.out.persistence.ArticlePersistenceAdapter
import com.paul9834.domain.model.Article
import com.paul9834.domain.port.`in`.NewsUseCase
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class NewsService(
    private val persistenceAdapter: ArticlePersistenceAdapter
) : NewsUseCase {

    @Cacheable(value = ["news"], key = "(#category ?: 'all') + '_' + #page + '_' + #size")
    override fun getArticles(category: String?, page: Int, size: Int): List<Article> {
        return persistenceAdapter.findArticles(category, page, size)
    }

    @Cacheable(value = ["admin-news"], key = "(#category ?: 'all') + '_' + #page + '_' + #size")
    override fun getAdminArticles(category: String?, page: Int, size: Int): List<Article> {
        return persistenceAdapter.findAdminArticles(category, page, size)
    }

    @Cacheable(value = ["article"], key = "#slug")
    override fun getArticleBySlug(slug: String): Article? {
        return persistenceAdapter.findBySlug(slug)
    }

    @CacheEvict(value = ["news", "admin-news", "article"], allEntries = true)
    override fun createArticle(article: Article): Article {
        return persistenceAdapter.save(article)
    }

    @CacheEvict(value = ["news", "admin-news", "article"], allEntries = true)
    override fun updateArticle(slug: String, article: Article): Article {
        return persistenceAdapter.update(slug, article)
    }

    @CacheEvict(value = ["news", "admin-news", "article"], allEntries = true)
    override fun deleteArticle(slug: String) {
        persistenceAdapter.deleteBySlug(slug)
    }

    @CacheEvict(value = ["news", "admin-news", "article"], allEntries = true)
    override fun publishArticle(slug: String): Article {
        return persistenceAdapter.publish(slug)
    }

    @CacheEvict(value = ["news", "admin-news", "article"], allEntries = true)
    override fun likeArticle(slug: String): Article {
        val current = persistenceAdapter.findBySlug(slug)
            ?: error("Article not found: $slug")

        val updated = current.copy(likesCount = current.likesCount + 1)
        return persistenceAdapter.update(slug, updated)
    }


}
