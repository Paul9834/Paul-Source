package com.paul9834.application

import com.paul9834.domain.model.Article
import com.paul9834.domain.port.`in`.NewsUseCase
import com.paul9834.domain.port.out.NewsRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class NewsService(
    private val newsRepository: NewsRepository
) : NewsUseCase {

    @Cacheable(value = ["news"], key = "#topic + '_' + #page + '_' + #size")
    override fun getTopNews(topic: String, page: Int, size: Int): List<Article> {
        val allArticles = newsRepository.fetchTopNews(topic, maxResults = 100)
        return allArticles
            .drop(page * size)
            .take(size)
    }

    @Cacheable(value = ["article"], key = "#slug")
    override fun getArticleBySlug(slug: String): Article? {
        val allArticles = newsRepository.fetchTopNews(topic = "technology", maxResults = 100)
        return allArticles.find { it.slug == slug }
    }
}
