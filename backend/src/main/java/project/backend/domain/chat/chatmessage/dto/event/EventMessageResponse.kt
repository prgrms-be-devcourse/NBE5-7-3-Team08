package project.backend.domain.chat.chatmessage.dto.event

import project.backend.domain.chat.chatmessage.entity.MessageType
import java.time.LocalDateTime

data class EventMessageResponse(
    val messageId: Long? = null,
    val type: MessageType? = null,
    val sender: String? = null,
    val roomId: Long? = null,
    val content: String? = null,
    val sendAt: LocalDateTime? = null
)

