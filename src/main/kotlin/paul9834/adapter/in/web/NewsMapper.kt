package adapter.`in`.web

import adapter.`in`.web.dto.ArticleResponse
import com.paul9834.paul9834.domain.model.Article

object NewsMapper {

    fun toResponse(article: Article): ArticleResponse {
        return ArticleResponse(
            slug = article.slug,
            title = article.title,
            description = article.description,
            content = article.content,
            url = article.url,
            imageUrl = article.imageUrl,
            publishedAt = article.publishedAt,
            sourceName = article.sourceName,
            sourceUrl = article.sourceUrl
        )
    }
}
