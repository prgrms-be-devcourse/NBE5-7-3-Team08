package project.backend.domain.member.entity

import jakarta.persistence.*
import lombok.*
import org.springframework.security.crypto.password.PasswordEncoder
import project.backend.domain.chat.chatroom.entity.ChatParticipant
import java.time.LocalDateTime

@Entity
class Member (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    var username: String,

    @Column(nullable = false)
    var nickname: String,

    @Column(unique = true)
    var email: String? = null,

    var password: String? = null,

    @Column(updatable = false)
    @Enumerated(EnumType.STRING)
    var provider: ProviderType,

    val joinAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "participant")
    val participants: List<ChatParticipant> = emptyList(),

    @Column(nullable = false)
    var profileImage: String,

    var recentRoomId: Long? = null
) {

    fun updateNickname(nickname: String) {
        this.nickname = nickname
    }

    fun updateEmail(email: String?) {
        this.email = email
    }

    fun updatePassword(password: String, encoder: PasswordEncoder) {
        this.password = encoder.encode(password)
    }

    fun updateProfileImage(profileImage: String) {
        this.profileImage = profileImage
    }

}