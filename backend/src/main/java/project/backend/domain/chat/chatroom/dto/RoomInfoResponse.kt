package project.backend.domain.chat.chatroom.dto

data class RoomInfoResponse (
     val roomId: Long,

     val roomName: String,

     val repositoryUrl: String,

     val inviteCode: String
)




