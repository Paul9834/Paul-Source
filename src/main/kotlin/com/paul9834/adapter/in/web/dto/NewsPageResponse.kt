package adapter.`in`.web.dto

data class NewsPageResponse(
    val articles: List<adapter.`in`.web.dto.ArticleResponse>,
    val page: Int,
    val size: Int,
    val topic: String
)
