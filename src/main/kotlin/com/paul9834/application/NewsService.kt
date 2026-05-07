package com.paul9834.application

import com.paul9834.adapter.out.persistence.ArticlePersistenceAdapter
import com.paul9834.paul9834.domain.model.Article

import com.paul9834.paul9834.domain.port.`in`.NewsUseCase
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class NewsService(
    private val persistenceAdapter: ArticlePersistenceAdapter
) : NewsUseCase {

    @Cacheable(value = ["news"], key = "#topic + '_' + #page + '_' + #size")
    override fun getTopNews(topic: String, page: Int, size: Int): List<Article> {
        return persistenceAdapter.fetchTopNews(topic, maxResults = page * size + size)
            .drop(page * size)
            .take(size)
    }

    @Cacheable(value = ["article"], key = "#slug")
    override fun getArticleBySlug(slug: String): Article? {
        return persistenceAdapter.findBySlug(slug)
    }
}
