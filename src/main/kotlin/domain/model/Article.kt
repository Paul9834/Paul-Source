package com.paul9834.domain.model

data class Article(
    val slug: String,
    val title: String,
    val description: String,
    val content: String,
    val url: String,
    val imageUrl: String?,
    val publishedAt: String,
    val sourceName: String,
    val sourceUrl: String
)
