package project.backend.domain.imagefile

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import project.backend.global.exception.errorcode.ImageFileErrorCode
import project.backend.global.exception.ex.ImageFileException
import java.io.IOException
import java.util.*

@Service
class ImageFileService(
    private val imageFileRepository: ImageFileRepository,
    private val amazonS3: AmazonS3
) {

    @field: Value("\${file.images.profile.path}")
    private lateinit var profilePath: String

    @field: Value("\${file.images.chat.path}")
    private lateinit var chatImagePath: String

    @field: Value("\${cloud.aws.s3.bucket}")
    private lateinit var bucket: String

    @Transactional
    fun saveChatImage(file: MultipartFile): ImageFile {
        val originalFilename = file.originalFilename
        val storeFileName = uploadImageToS3(file, chatImagePath)
        val imageFile = ImageFile.of(storeFileName, originalFilename)
        return imageFileRepository.save(imageFile)
    }

    @Transactional
    fun saveProfileImage(file: MultipartFile): String =
        uploadImageToS3(file, profilePath)

    @Transactional
    protected fun uploadImageToS3(file: MultipartFile, s3Path: String): String {
        val storeFileName = genStorageFileName(file)
        val s3Key = "$s3Path$storeFileName"

        try {
            val metadata = ObjectMetadata().apply {
                contentType = file.contentType
                contentLength = file.size
            }

            amazonS3.putObject(PutObjectRequest(bucket, s3Key, file.inputStream, metadata))
            println("[S3] 이미지 업로드 : $s3Key")

        } catch (e: IOException) {
            throw ImageFileException(ImageFileErrorCode.FILE_SAVE_FAILURE, e)
        } catch (e: AmazonServiceException) {
            throw ImageFileException(ImageFileErrorCode.FILE_SAVE_FAILURE, e)
        }

        return storeFileName
    }

    private fun genStorageFileName(file: MultipartFile): String {
        val originalFilename = file.originalFilename ?: throw ImageFileException(ImageFileErrorCode.INVALID_IMAGE_TYPE)

        validateFileName(originalFilename)
        checkFileTypeIsImage(file.contentType)
        val extension = originalFilename.substringAfterLast('.', "").lowercase()

        checkFileExtensionIsImage(".$extension")

        return "${UUID.randomUUID()}.$extension"
    }

    private fun checkFileExtensionIsImage(extension: String) {
        val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp")
        if (extension !in imageExtensions) {
            throw ImageFileException(ImageFileErrorCode.INVALID_IMAGE_TYPE)
        }
    }

    private fun checkFileTypeIsImage(fileType: String?) {
        if (fileType == null || !fileType.startsWith("image/")) {
            throw ImageFileException(ImageFileErrorCode.INVALID_IMAGE_TYPE)
        }
    }

    private fun validateFileName(fileName: String) {
        if (!fileName.contains('.')) {
            throw ImageFileException(ImageFileErrorCode.INVALID_IMAGE_TYPE)
        }
    }

    @Transactional(readOnly = true)
    fun getImageById(imageFileId: Long): ImageFile =
        imageFileRepository.findById(imageFileId).orElseThrow {
            ImageFileException(ImageFileErrorCode.FILE_NOT_FOUND)
        }
}