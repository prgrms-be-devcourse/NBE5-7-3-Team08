package project.backend.domain.chat.chatmessage.dto

import project.backend.domain.chat.chatmessage.entity.MessageType

data class ChatMessageRequest(
    val content: String? = null,
    val type: MessageType,
    val language: String?= null,
    val imageFileId: Long? = null
)