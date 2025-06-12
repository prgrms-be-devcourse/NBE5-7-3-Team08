package project.backend.domain.chat.chatroom.dto

import jakarta.validation.constraints.NotBlank

data class ChatRoomRequest (

    @field:NotBlank(message = "채팅방 이름을 설정해주세요")
    val name:  String,

    //	@NotBlank(message = "채팅방 레포지토리주소를 설정해주세요")
    val repositoryUrl: String
)
