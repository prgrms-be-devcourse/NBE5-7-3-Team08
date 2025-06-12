package project.backend.domain.chat.chatmessage.entity

import jakarta.persistence.*
import project.backend.domain.chat.chatroom.entity.ChatRoom
import project.backend.domain.imagefile.ImageFile
import project.backend.domain.member.entity.Member
import java.time.LocalDateTime

@Entity
@Table(name = "chat_message")
class ChatMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    val sender: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    val chatRoom: ChatRoom,

    @Lob
    @Column(columnDefinition = "TEXT")
    var content: String? = null,

    val sendAt: LocalDateTime,

    @Enumerated(EnumType.STRING)
    val type: MessageType,

    var codeLanguage: String? = null,

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "chat_image_id")
    val chatImage: ImageFile? = null,

    @Enumerated(EnumType.STRING)
    var status: MessageStatus = MessageStatus.NO_CHANGE
) {
    protected constructor() : this(
        sender = Member(),
        chatRoom = ChatRoom(),
        sendAt = LocalDateTime.now(),
        type = MessageType.TEXT
    )

    fun updateContent(newContent: String?) {
        newContent?.let {
            content = it
            status = MessageStatus.EDITED
        }
    }

    fun updateLanguage(language: String?) {
        language?.let {
            codeLanguage = it
            status = MessageStatus.EDITED
        }
    }

    fun delete() {
        status = MessageStatus.DELETED
    }
}