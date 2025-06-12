package project.backend.domain.chat.chatmessage.entity

import jakarta.persistence.*
import project.backend.domain.chat.chatroom.entity.ChatRoom
import project.backend.domain.imagefile.ImageFile
import project.backend.domain.member.entity.Member
import java.time.LocalDateTime

@Entity
class ChatMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "member_id")
    var sender: Member,

    @ManyToOne
    @JoinColumn(name = "room_id")
    var chatRoom: ChatRoom,

    @Lob
    @Column(columnDefinition = "TEXT")
    var content: String? = null,

    var sendAt: LocalDateTime,

    @Enumerated(EnumType.STRING)
    var type: MessageType? = null,

    var codeLanguage: String? = null,

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "chat_image_id")
    var chatImage: ImageFile? = null,

    @Enumerated(EnumType.STRING)
    var status: MessageStatus = MessageStatus.NO_CHANGE
) {

    fun updateContent(newContent: String?) {
        if (newContent != null) {
            content = newContent
            status = MessageStatus.EDITED
        }
    }

    fun updateLanguage(language: String?) {
        if (language != null) {
            codeLanguage = language
            status = MessageStatus.EDITED
        }
    }

    fun delete() {
        status = MessageStatus.DELETED
    }
}