package project.backend.domain.chat.chatroom.dto

//@JvmRecord
data class EntryRoomResponse(
    val roomId: Long,
    val roomName: String,
    val ownerId: Long
)
