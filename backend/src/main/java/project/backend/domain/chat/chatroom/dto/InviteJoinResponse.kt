package project.backend.domain.chat.chatroom.dto

data class InviteJoinResponse(
    val id: Long,
    val inviteCode: String,
    val name: String
)
