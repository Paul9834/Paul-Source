package adapter.`in`.web.dto

data class ArticleResponse(
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