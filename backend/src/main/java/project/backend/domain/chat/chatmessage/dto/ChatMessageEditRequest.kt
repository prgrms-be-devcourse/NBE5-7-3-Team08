package project.backend.domain.chat.chatmessage.dto

import project.backend.domain.chat.chatmessage.entity.MessageType

data class ChatMessageEditRequest(
    val messageId: Long,
    val content: String,
    val type: MessageType?,
    val language: String?
)
