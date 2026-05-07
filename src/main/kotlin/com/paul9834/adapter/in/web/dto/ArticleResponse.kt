package com.paul9834.adapter.`in`.web.dto

data class ArticleResponse(
    val slug: String,
    val title: String,
    val description: String,
    val content: String,
    val imageUrl: String?,
    val category: String,
    val published: Boolean,
    val publishedAt: String
)
