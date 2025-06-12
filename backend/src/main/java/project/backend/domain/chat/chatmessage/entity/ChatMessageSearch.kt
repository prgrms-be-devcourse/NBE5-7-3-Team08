package project.backend.domain.chat.chatmessage.entity

import jakarta.persistence.*

@Entity
@Table(name = "chat_message_search")
class ChatMessageSearch(
    @Id
    val id: Long,

    @Column(name = "room_id", nullable = false)
    val roomId: Long,

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String
) {

    fun updateContent(content: String) {
        this.content = content
    }

    fun deleteContent() {
        this.content = ""
    }
}