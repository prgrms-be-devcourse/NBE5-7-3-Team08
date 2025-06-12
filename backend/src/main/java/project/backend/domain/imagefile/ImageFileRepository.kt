package project.backend.domain.imagefile

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ImageFileRepository : JpaRepository<ImageFile, Long> {

    fun existsByStoreFileName(imgName: String): Boolean

    fun findByUploadFileName(uploadFileName: String): Optional<ImageFile>

    fun findByStoreFileName(storeFileName: String): Optional<ImageFile>
}