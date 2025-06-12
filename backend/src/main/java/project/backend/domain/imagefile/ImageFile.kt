package project.backend.domain.imagefile

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class ImageFile(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val imageId: Long? = null,

    @Column(nullable = false)
    val storeFileName: String,

    @Column(nullable = false)
    val uploadFileName: String

) {
    companion object {
        fun of(storeFileName: String, uploadFileName: String) =
            ImageFile(storeFileName = storeFileName, uploadFileName = uploadFileName)
    }
}