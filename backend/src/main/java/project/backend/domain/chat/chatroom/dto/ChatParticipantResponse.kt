package project.backend.domain.chat.chatroom.dto

data class ChatParticipantResponse(
    val memberId: Long,

    val nickname: String,

    val profileImageUrl: String,

    val owner: Boolean
)

