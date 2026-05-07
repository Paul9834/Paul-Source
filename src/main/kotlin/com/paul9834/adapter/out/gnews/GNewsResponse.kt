package com.paul9834.paul9834.adapter.out.gnews

data class GNewsResponse(
    val totalArticles: Int,
    val articles: List<GNewsArticleDto>
)

data class GNewsArticleDto(
    val title: String,
    val description: String?,
    val content: String?,
    val url: String,
    val image: String?,
    val publishedAt: String,
    val source: GNewsSourceDto
)

data class GNewsSourceDto(
    val name: String,
    val url: String
)
