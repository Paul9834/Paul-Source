package com.paul9834.application

import com.paul9834.adapter.out.gnews.GNewsClient
import com.paul9834.adapter.out.persistence.ArticlePersistenceAdapter
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class NewsSyncService(
    private val gNewsClient: GNewsClient,
    private val persistenceAdapter: ArticlePersistenceAdapter
) {
    @Scheduled(fixedRate = 3_600_000, initialDelay = 0)
    fun syncNews() {
        listOf("technology", "science", "world", "business").forEach { topic ->
            try {
                val articles = gNewsClient.fetchTopNews(topic, maxResults = 10)
                persistenceAdapter.saveAll(articles)
                println("✅ Synced $topic: ${articles.size} articles")
            } catch (e: Exception) {
                println("❌ Error syncing $topic: ${e.message}")
            }
        }
    }
}
