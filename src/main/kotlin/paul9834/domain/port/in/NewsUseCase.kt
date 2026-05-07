package com.paul9834.paul9834.domain.port.`in`

import com.paul9834.paul9834.domain.model.Article


interface NewsUseCase {
    fun getTopNews(topic: String, page: Int, size: Int): List<Article>
    fun getArticleBySlug(slug: String): Article?
}
