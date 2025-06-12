package project.backend.domain.chat.chatmessage.mapper

import ChatMessageResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import project.backend.domain.chat.chatmessage.dto.*
import project.backend.domain.chat.chatmessage.entity.*
import project.backend.domain.chat.chatroom.dto.event.JoinChatRoomEvent
import project.backend.domain.chat.chatroom.dto.event.LeaveChatRoomEvent
import project.backend.domain.chat.chatroom.entity.ChatRoom
import project.backend.domain.chat.github.dto.GitMessageDto
import project.backend.domain.imagefile.ImageFile
import project.backend.domain.member.entity.Member
import java.time.LocalDateTime

@Component
class ChatMessageMapper {

    @Value("\${file.images.profile.github}")
    private lateinit var githubProfile: String

    fun toEntityWithText(
        room: ChatRoom,
        sender: Member,
        request: ChatMessageRequest
    ): ChatMessage {
        return ChatMessage(
            chatRoom = room,
            sender = sender,
            content = request.content,
            type = MessageType.TEXT,
            sendAt = LocalDateTime.now()
        )
    }

    fun toEntityWithCode(
        room: ChatRoom,
        sender: Member,
        request: ChatMessageRequest
    ): ChatMessage {
        return ChatMessage(
            chatRoom = room,
            sender = sender,
            content = request.content,
            type = MessageType.CODE,
            sendAt = LocalDateTime.now(),
            codeLanguage = request.language
        )
    }

    fun toEntityWithImage(
        room: ChatRoom,
        sender: Member,
        chatImage: ImageFile
    ): ChatMessage {
        return ChatMessage(
            chatRoom = room,
            sender = sender,
            type = MessageType.IMAGE,
            sendAt = LocalDateTime.now(),
            chatImage = chatImage
        )
    }

    fun toEntityWithGit(gitMessage: GitMessageDto, githubBot: Member): ChatMessage {
        return ChatMessage(
            chatRoom = gitMessage.room,
            type = MessageType.GIT,
            content = gitMessage.content,
            sendAt = LocalDateTime.now(),
            sender = githubBot
        )
    }

    fun toEntityWithJoinEvent(
        room: ChatRoom,
        sender: Member,
        joinEvent: JoinChatRoomEvent
    ): ChatMessage {
        return ChatMessage(
            chatRoom = room,
            sender = sender,
            content = "${joinEvent.nickname}님이 입장했습니다.",
            type = MessageType.EVENT,
            sendAt = joinEvent.joinAt
        )
    }

    fun toEntityWithLeaveEvent(
        room: ChatRoom,
        sender: Member,
        leaveEvent: LeaveChatRoomEvent
    ): ChatMessage {
        return ChatMessage(
            chatRoom = room,
            sender = sender,
            content = "${leaveEvent.nickname}님이 나갔습니다.",
            type = MessageType.EVENT,
            sendAt = leaveEvent.leaveAt
        )
    }

    // 저장된 메시지에서 ID, roomId, content만 꺼내서 저장하므로 ChatMessage 사용
    fun toSearchEntity(message: ChatMessage): ChatMessageSearch {
        return ChatMessageSearch(
            id = message.id,
            roomId = message.chatRoom.id,
            content = message.content ?: ""
        )
    }

    fun toResponse(message: ChatMessage): ChatMessageResponse {
        val senderName = message.sender.nickname

        return ChatMessageResponse(
            senderName = senderName,
            content = message.content,
            type = message.type,
            sendAt = message.sendAt,
            language = message.codeLanguage,
            profileImageUrl = message.sender.profileImage,
            chatImageUrl = message.chatImage?.storeFileName,
            senderId = message.sender.id,
            messageId = message.id,
            status = message.status
        )
    }

    fun toSearchResponse(message: ChatMessage): ChatMessageSearchResponse {
        return ChatMessageSearchResponse(
            messageId = message.id,
            content = message.content,
            senderName = message.sender.nickname,
            profileImageUrl = message.sender.profileImage,
            sendAt = message.sendAt,
            type = message.type
        )
    }

    fun toGitResponse(message: ChatMessage): ChatMessageResponse {
        return ChatMessageResponse(
            senderName = "깃허브봇",
            content = message.content,
            type = message.type,
            sendAt = message.sendAt,
            messageId = message.id,
            profileImageUrl = githubProfile,
            language = null,
            chatImageUrl = null,
            senderId = message.sender.id,
            status = message.status
        )
    }
}