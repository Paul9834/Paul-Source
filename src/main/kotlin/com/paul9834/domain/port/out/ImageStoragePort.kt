package com.paul9834.domain.port.out

import org.springframework.web.multipart.MultipartFile

interface ImageStoragePort {
    fun uploadNewsImage(file: MultipartFile): String
}
