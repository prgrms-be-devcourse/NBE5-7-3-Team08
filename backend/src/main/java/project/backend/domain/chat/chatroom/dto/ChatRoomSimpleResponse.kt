package project.backend.domain.chat.chatroom.dto

class ChatRoomSimpleResponse (
    val id: Long,
    val name: String,
    val repositoryUrl: String,
    val ownerId: Long,
    val inviteCode: String,
)