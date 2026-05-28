package com.paul9834.adapter.out.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ArticleJpaRepository : JpaRepository<ArticleEntity, Long> {
    fun findBySlug(slug: String): ArticleEntity?
    fun existsBySlug(slug: String): Boolean
    fun deleteBySlug(slug: String)

    fun findByPublishedTrueOrderByPublishedAtDesc(pageable: Pageable): Page<ArticleEntity>
    fun findByPublishedTrueAndCategoryOrderByPublishedAtDesc(
        category: String,
        pageable: Pageable
    ): Page<ArticleEntity>

    fun findAllByOrderByCreatedAtDesc(pageable: Pageable): Page<ArticleEntity>
    fun findByCategoryOrderByCreatedAtDesc(
        category: String,
        pageable: Pageable
    ): Page<ArticleEntity>
}
