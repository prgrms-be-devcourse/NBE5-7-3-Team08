package project.backend.domain.chat.chatmessage.dto

import ChatMessageResponse

data class ChatScrollResponse(
    val messages: List<ChatMessageResponse>,
    val nextCursor: Long?
)