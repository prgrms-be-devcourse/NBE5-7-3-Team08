package project.backend.domain.chat.chatmessage.dto

import project.backend.domain.chat.chatmessage.entity.MessageType
import java.time.LocalDateTime

data class ChatMessageSearchResponse(
    val messageId: Long,
    val content: String? = null,
    val type: MessageType,
    val senderName: String,
    val profileImageUrl: String? = null,
    val sendAt: LocalDateTime
)

