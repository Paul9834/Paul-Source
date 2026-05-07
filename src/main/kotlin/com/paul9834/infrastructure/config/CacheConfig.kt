package com.paul9834.infrastructure.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class CacheConfig {

    @Bean
    fun cacheManager(): SimpleCacheManager {
        val newsCache = CaffeineCache(
            "news",
            Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(50)
                .build()
        )
        val articleCache = CaffeineCache(
            "article",
            Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(200)
                .build()
        )
        return SimpleCacheManager().apply {
            setCaches(listOf(newsCache, articleCache))
        }
    }
}
