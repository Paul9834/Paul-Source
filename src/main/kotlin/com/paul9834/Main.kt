package com.paul9834.paul9834

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class PaulSourceApplication

fun main(args: Array<String>) {
    runApplication<com.paul9834.paul9834.PaulSourceApplication>(*args)
}
