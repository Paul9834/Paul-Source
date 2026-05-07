package com.paul9834.paul9834.adapter.out.gnews

import com.paul9834.paul9834.domain.model.Article
import com.paul9834.paul9834.domain.port.out.NewsRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class GNewsClient(
    private val webClient: WebClient,
    @Value("\${gnews.api.key}") private val apiKey: String
) : NewsRepository {

    override fun fetchTopNews(topic: String, maxResults: Int): List<Article> {
        return try {
            val response = webClient.get()
                .uri { builder ->
                    builder
                        .path("/top-headlines")
                        .queryParam("topic", topic)
                        .queryParam("max", maxResults.coerceAtMost(10)) // plan free: max 10
                        .queryParam("lang", "en")
                        .queryParam("apikey", apiKey)
                        .build()
                }
                .retrieve()
                .bodyToMono<GNewsResponse>()
                .block()

            response?.articles?.map { GNewsMapper.toDomain(it) } ?: emptyList()

        } catch (e: Exception) {
            println("Error fetching news from GNews: ${e.message}")
            emptyList()
        }
    }
}
