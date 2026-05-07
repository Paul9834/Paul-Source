package com.paul9834.adapter.`in`.web.dto

data class ArticleRequest(
    val title: String,
    val description: String,
    val content: String,
    val imageUrl: String? = null,
    val category: String = "general",
    val published: Boolean = false
)
