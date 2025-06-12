package project.backend.domain.chat.chatroom.mapper

import org.springframework.stereotype.Component
import project.backend.domain.chat.chatmessage.dto.event.EventMessageResponse
import project.backend.domain.chat.chatmessage.entity.MessageType
import project.backend.domain.chat.chatroom.dto.*
import project.backend.domain.chat.chatroom.dto.event.DeleteChatRoomEvent
import project.backend.domain.chat.chatroom.dto.event.JoinChatRoomEvent
import project.backend.domain.chat.chatroom.dto.event.LeaveChatRoomEvent
import project.backend.domain.chat.chatroom.entity.ChatParticipant
import project.backend.domain.chat.chatroom.entity.ChatRoom
import project.backend.domain.member.entity.Member
import java.time.LocalDateTime
import java.util.*

@Component
class ChatRoomMapper {

    //임창인: 간단 응답 변환
    fun toSimpleResponse(entity: ChatRoom, owner: Member): ChatRoomSimpleResponse {
        return ChatRoomSimpleResponse(
            entity.id!!,
            entity.name,
            entity.repositoryUrl,
            owner.id!!,
            entity.inviteCode
        )
    }

    // 임창인 엔티티 변환
    fun toEntity(dto: ChatRoomRequest): ChatRoom {
        return ChatRoom(
            null,
            dto.name,
            dto.repositoryUrl,
            LocalDateTime.now(),
            generateInviteCode())
    }

    companion object {
        fun toListResponse(chatRoom: ChatRoom): RoomInfoResponse {
            return RoomInfoResponse(
                chatRoom.id!!,
                chatRoom.name,
                chatRoom.repositoryUrl,
                chatRoom.inviteCode
            )
        }

        // 강현님: 참여자 응답 변환
        fun toParticipantResponse(p: ChatParticipant): ChatParticipantResponse {
            return ChatParticipantResponse(
                p.participant.id!!,
                p.participant.nickname,
                p.participant.profileImage,
                p.isOwner
            )
        }

        //문성이꺼
        fun toProfileResponse(chatRoom: ChatRoom): MyChatRoomResponse {
            return MyChatRoomResponse(
                chatRoom.id!!,
                chatRoom.name,
                chatRoom.getActiveParticipantCount(),
                chatRoom.inviteCode
            )
        }

        fun toInviteJoinResponse(
            id: Long,
            inviteCode: String,
            name: String
        ): InviteJoinResponse {
            return InviteJoinResponse(
                id,
                inviteCode,
                name
            )
        }

        private fun generateInviteCode(): String {
            return UUID.randomUUID().toString()
        }

        fun toJoinEventMessageResponse(
            joinEvent: JoinChatRoomEvent,
            messageId: Long
        ): EventMessageResponse {
            return EventMessageResponse(
                messageId,
                MessageType.EVENT,
                joinEvent.nickname,
                joinEvent.roomId,
                joinEvent.nickname + "님이 입장했습니다.",
                joinEvent.joinAt)

        }

        fun toLeaveEventMessageResponse(
            leaveEvent: LeaveChatRoomEvent,
            messageId: Long
        ): EventMessageResponse {
            return EventMessageResponse(
                messageId,
                MessageType.EVENT,
                leaveEvent.nickname,
                leaveEvent.roomId,
                leaveEvent.nickname + "님이 나갔습니다.",
                leaveEvent.leaveAt
            )
        }

        fun toDeleteEventMessageResponse(deleteEvent: DeleteChatRoomEvent): EventMessageResponse {
            return EventMessageResponse(
                null,
                MessageType.EVENT,
                "System",
                deleteEvent.roomId,
                "채팅방 '" + deleteEvent.roomName + "'이 삭제되었습니다.",
            )
        }
    }
}
