package com.paul9834.adapter.out.persistence

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "articles")
class ArticleEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false, length = 100)
    val slug: String = "",

    @Column(nullable = false, length = 500)
    val title: String = "",

    @Column(columnDefinition = "TEXT")
    val description: String = "",

    @Column(columnDefinition = "LONGTEXT")
    val content: String = "",

    @Column(nullable = false, length = 1000)
    val url: String = "",

    @Column(length = 1000)
    val imageUrl: String? = null,

    @Column(nullable = false)
    val publishedAt: String = "",

    @Column(length = 200)
    val sourceName: String = "",

    @Column(length = 500)
    val sourceUrl: String = "",

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
