package project.backend.domain.chat.chatroom.dto

data class MyChatRoomResponse(
    val roomId: Long,
    val roomName: String,
    val participantCount: Int,
    val inviteCode: String
)
