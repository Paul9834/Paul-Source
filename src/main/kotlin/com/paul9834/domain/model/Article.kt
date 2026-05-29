package com.paul9834.domain.model

// Article.kt
data class Article(
    val slug: String,
    val title: String,
    val description: String,
    val content: String,
    val imageUrl: String?,
    val category: String,
    val published: Boolean,
    val publishedAt: String,
    val createdAt: String = "",
    val likesCount: Long = 0
)
