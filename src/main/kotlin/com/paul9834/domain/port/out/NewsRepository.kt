package com.paul9834.paul9834.domain.port.out

import com.paul9834.paul9834.domain.model.Article

interface NewsRepository {
    fun fetchTopNews(topic: String, maxResults: Int): List<Article>
}
