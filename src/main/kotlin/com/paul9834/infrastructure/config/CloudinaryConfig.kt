package com.paul9834.infrastructure.config

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CloudinaryConfig {

    @Bean
    fun cloudinary(
        @Value("\${cloudinary.cloud-name}") cloudName: String,
        @Value("\${cloudinary.api-key}") apiKey: String,
        @Value("\${cloudinary.api-secret}") apiSecret: String
    ): Cloudinary {
        return Cloudinary(
            ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
            )
        )
    }
}
