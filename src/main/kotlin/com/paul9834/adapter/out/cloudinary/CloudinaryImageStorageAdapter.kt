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
        val uploadResult = cloudinary.uploader().upload(
            file.bytes,
            ObjectUtils.asMap(
                "folder", "paul9834/news"
            )
        )

        return uploadResult["secure_url"] as String
    }
}
