package com.paul9834.adapter.`in`.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.paul9834.adapter.`in`.web.dto.ArticleRequest
import com.paul9834.domain.model.Article
import com.paul9834.domain.port.`in`.NewsUseCase
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(NewsController::class)
@Import(NewsControllerTest.Config::class)
class NewsControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var newsUseCase: FakeNewsUseCase

    @BeforeEach
    fun resetFake() {
        newsUseCase.reset()
    }

    @Test
    fun `returns paged articles by category`() {
        newsUseCase.articles = listOf(article())

        mockMvc.perform(
            get("/api/news")
                .param("category", "technology")
                .param("page", "0")
                .param("size", "2")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.topic").value("technology"))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(2))
            .andExpect(jsonPath("$.articles", hasSize<Any>(1)))
            .andExpect(jsonPath("$.articles[0].slug").value("spring-news"))
            .andExpect(jsonPath("$.articles[0].category").value("technology"))
            .andExpect(jsonPath("$.articles[0].published").value(true))
    }

    @Test
    fun `returns article by slug`() {
        newsUseCase.articlesBySlug["spring-news"] = article()

        mockMvc.perform(get("/api/news/spring-news"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.slug").value("spring-news"))
            .andExpect(jsonPath("$.title").value("Spring News"))
    }

    @Test
    fun `returns not found when article does not exist`() {
        mockMvc.perform(get("/api/news/missing"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `creates article`() {
        val request = ArticleRequest(
            title = "New Title",
            description = "Description",
            content = "Content",
            imageUrl = "https://example.com/image.jpg",
            category = "technology",
            published = false
        )
        val expected = Article(
            slug = "new-title",
            title = "New Title",
            description = "Description",
            content = "Content",
            imageUrl = "https://example.com/image.jpg",
            category = "technology",
            published = false,
            publishedAt = ""
        )
        mockMvc.perform(
            post("/api/news")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.slug").value("new-title"))
            .andExpect(jsonPath("$.published").value(false))
    }

    @Test
    fun `publishes article`() {
        val published = article().copy(published = true, publishedAt = "2026-05-25T18:00:00")
        newsUseCase.publishedArticlesBySlug["spring-news"] = published

        mockMvc.perform(patch("/api/news/spring-news/publish"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.slug").value("spring-news"))
            .andExpect(jsonPath("$.published").value(true))
            .andExpect(jsonPath("$.publishedAt").value("2026-05-25T18:00:00"))
    }

    @Test
    fun `deletes article`() {
        mockMvc.perform(delete("/api/news/spring-news"))
            .andExpect(status().isNoContent)

        assertTrue(newsUseCase.deletedSlugs.contains("spring-news"))
    }

    private fun article() = Article(
        slug = "spring-news",
        title = "Spring News",
        description = "Description",
        content = "Content",
        imageUrl = "https://example.com/image.jpg",
        category = "technology",
        published = true,
        publishedAt = "2026-05-25T18:00:00",
        createdAt = "2026-05-25T18:00:00"
    )

    @TestConfiguration
    class Config {
        @Bean
        fun newsUseCase() = FakeNewsUseCase()
    }

    class FakeNewsUseCase : NewsUseCase {
        var articles: List<Article> = emptyList()
        val articlesBySlug = mutableMapOf<String, Article>()
        val publishedArticlesBySlug = mutableMapOf<String, Article>()
        val deletedSlugs = mutableListOf<String>()

        fun reset() {
            articles = emptyList()
            articlesBySlug.clear()
            publishedArticlesBySlug.clear()
            deletedSlugs.clear()
        }

        override fun getArticles(category: String?, page: Int, size: Int): List<Article> = articles

        override fun getArticleBySlug(slug: String): Article? = articlesBySlug[slug]

        override fun createArticle(article: Article): Article = article

        override fun updateArticle(slug: String, article: Article): Article = article

        override fun deleteArticle(slug: String) {
            deletedSlugs.add(slug)
        }

        override fun publishArticle(slug: String): Article {
            return publishedArticlesBySlug[slug] ?: error("Article not found: $slug")
        }
    }
}
