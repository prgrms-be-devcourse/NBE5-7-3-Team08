package project.backend.domain.chat.chatroom.dto

//강현님꺼
data class ChatRoomDetailResponse (
     val roomId: Long,

     val roomName: String,

     val ownerId: Long,

     val participantCount: Int,

     val repositoryUrl: String? = null,

     val participants: List<ChatParticipantResponse>
)


