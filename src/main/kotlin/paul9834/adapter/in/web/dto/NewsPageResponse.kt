package adapter.`in`.web.dto

data class NewsPageResponse(
    val articles: List<ArticleResponse>,
    val page: Int,
    val size: Int,
    val topic: String
)
