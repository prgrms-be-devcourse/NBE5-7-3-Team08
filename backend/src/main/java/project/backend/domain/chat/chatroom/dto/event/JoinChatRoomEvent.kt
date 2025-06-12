package project.backend.domain.chat.chatroom.dto.event

import java.time.LocalDateTime

data class JoinChatRoomEvent(
    val roomId: Long,
    val memberId: Long,
    val nickname: String,
    val joinAt: LocalDateTime
)

