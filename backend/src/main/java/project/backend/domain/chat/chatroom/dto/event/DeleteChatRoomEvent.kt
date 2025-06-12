package project.backend.domain.chat.chatroom.dto.event

data class DeleteChatRoomEvent(
	val roomId: Long,
	val roomName: String
)
