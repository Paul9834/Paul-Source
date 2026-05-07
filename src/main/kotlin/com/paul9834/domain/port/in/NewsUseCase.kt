package com.paul9834.domain.port.`in`

import com.paul9834.domain.model.Article


interface NewsUseCase {
    fun getArticles(category: String?, page: Int, size: Int): List<Article>
    fun getArticleBySlug(slug: String): Article?
    fun createArticle(article: Article): Article
    fun updateArticle(slug: String, article: Article): Article
    fun deleteArticle(slug: String)
    fun publishArticle(slug: String): Article
}
