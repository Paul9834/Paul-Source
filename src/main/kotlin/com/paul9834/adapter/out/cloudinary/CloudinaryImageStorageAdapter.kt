package com.paul9834.adapter.out.cloudinary

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.paul9834.domain.port.out.ImageStoragePort
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class CloudinaryImageStorageAdapter(
    private val cloudinary: Cloudinary
) : ImageStoragePort {

    override fun uploadNewsImage(file: MultipartFile): String {
        require(!file.isEmpty) { "La imagen está vacía" }
        require(file.contentType?.startsWith("image/") == true) { "El archivo debe ser una imagen" }
        require(file.size <= 5 * 1024 * 1024) { "La imagen no puede superar 5MB" }

        val result = cloudinary.uploader().upload(
            file.bytes,
            ObjectUtils.asMap(
                "folder", "paul9834/news",
                "resource_type", "image"
            )
        )

        return result["secure_url"] as String
    }
}
