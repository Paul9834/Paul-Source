package adapter.`in`.web

import adapter.`in`.web.dto.ArticleResponse
import com.paul9834.paul9834.domain.model.Article

object NewsMapper {

    fun toResponse(article: com.paul9834.paul9834.domain.model.Article): adapter.`in`.web.dto.ArticleResponse {
        return _root_ide_package_.adapter.`in`.web.dto.ArticleResponse(
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
