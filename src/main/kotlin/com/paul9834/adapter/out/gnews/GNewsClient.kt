package com.paul9834.adapter.out.gnews

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class GNewsClient(
    private val webClient: WebClient,
    @Value("\${gnews.api.key}") private val apiKey: String
) // ← SIN : NewsRepository
