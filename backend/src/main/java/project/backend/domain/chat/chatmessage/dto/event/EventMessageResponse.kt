package project.backend.domain.chat.chatmessage.dto.event

import project.backend.domain.chat.chatmessage.entity.MessageType
import java.time.LocalDateTime

class EventMessageResponse (
    val messageId: Long?,

    val type: MessageType,

    val sender: String,

    val roomId: Long,

    val content: String,

    val sendAt: LocalDateTime = LocalDateTime.now()
)

