package project.backend.domain.chat.chatroom.entity

import jakarta.persistence.*
import lombok.*
import project.backend.domain.chat.chatmessage.entity.ChatMessage
import java.time.LocalDateTime

@Entity
class ChatRoom(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    val id: Long? = null,

    @Column(nullable = false)
    val name: String,

    val repositoryUrl: String,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    val inviteCode: String,

    @OneToMany(mappedBy = "chatRoom", cascade = [CascadeType.ALL], orphanRemoval = true)
    val messages: MutableList<ChatMessage> = mutableListOf(),

    @OneToMany(mappedBy = "chatRoom", cascade = [CascadeType.ALL], orphanRemoval = true)
    val participants: MutableList<ChatParticipant> = mutableListOf()
) {

    fun addParticipant(chatParticipant: ChatParticipant) {
        participants.add(chatParticipant)
    }

    fun getActiveParticipantCount(): Int {
        return participants.count { it.isActive }
    }
}

