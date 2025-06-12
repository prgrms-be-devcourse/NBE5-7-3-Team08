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
import project.backend.domain.chat.chatroom.entity.ChatRoom.inviteCode
import project.backend.domain.chat.chatroom.entity.ChatRoom.name
import project.backend.domain.chat.chatroom.entity.ChatRoom.repositoryUrl
import project.backend.domain.member.entity.Member
import java.time.LocalDateTime
import java.util.*

@Component
class ChatRoomMapper {
    // 임창인: 간단 응답 변환
    fun toSimpleResponse(entity: ChatRoom, owner: Member): ChatRoomSimpleResponse {
        return ChatRoomSimpleResponse.of(
            entity.id,
            entity.name,
            entity.repositoryUrl,
            owner.id,
            entity.inviteCode
        )
    }


    // 임창인 엔티티 변환
    fun toEntity(dto: ChatRoomRequest): ChatRoom {
        return ChatRoom(null,
            dto.name,
            dto.repositoryUrl,
            LocalDateTime.now(),
            generateInviteCode())
    }

    companion object {
        fun toListResponse(chatRoom: ChatRoom): RoomInfoResponse {
            return RoomInfoResponse.builder()
                .roomId(chatRoom.id)
                .roomName(chatRoom.name)
                .repositoryUrl(chatRoom.repositoryUrl)
                .inviteCode(chatRoom.inviteCode)
                .build()
        }

        // 강현님: 참여자 응답 변환
        fun toParticipantResponse(p: ChatParticipant): ChatParticipantResponse {
            return ChatParticipantResponse.builder()
                .memberId(p.participant.id)
                .nickname(p.participant.nickname)
                .profileImageUrl(p.participant.profileImage)
                .isOwner(p.isOwner)
                .build()
        }


        //문성이꺼
        fun toProfileResponse(chatRoom: ChatRoom): MyChatRoomResponse {
            return MyChatRoomResponse.builder()
                .roomId(chatRoom.id)
                .roomName(chatRoom.name)
                .participantCount(chatRoom.getActiveParticipantCount())
                .inviteCode(chatRoom.inviteCode)
                .build()
        }

        fun toInviteJoinResponse(
            id: Long?,
            inviteCode: String?,
            name: String?
        ): InviteJoinResponse {
            return InviteJoinResponse.builder()
                .id(id)
                .inviteCode(inviteCode)
                .name(name)
                .build()
        }

        private fun generateInviteCode(): String {
            return UUID.randomUUID().toString()
        }

        fun toJoinEventMessageResponse(
            joinEvent: JoinChatRoomEvent,
            messageId: Long?
        ): EventMessageResponse {
            return EventMessageResponse.builder()
                .messageId(messageId)
                .type(MessageType.EVENT)
                .roomId(joinEvent.roomId)
                .sender(joinEvent.nickname)
                .content(joinEvent.nickname + "님이 입장했습니다.")
                .sendAt(joinEvent.joinAt)
                .build()
        }

        fun toLeaveEventMessageResponse(
            leaveEvent: LeaveChatRoomEvent,
            messageId: Long?
        ): EventMessageResponse {
            return EventMessageResponse.builder()
                .messageId(messageId)
                .type(MessageType.EVENT)
                .roomId(leaveEvent.roomId)
                .sender(leaveEvent.nickname)
                .content(leaveEvent.nickname + "님이 나갔습니다.")
                .sendAt(leaveEvent.leaveAt)
                .build()
        }

        fun toDeleteEventMessageResponse(deleteEvent: DeleteChatRoomEvent): EventMessageResponse {
            return EventMessageResponse.builder()
                .type(MessageType.EVENT)
                .roomId(deleteEvent.roomId)
                .content("채팅방 '" + deleteEvent.roomName + "'이 삭제되었습니다.")
                .build()
        }
    }
}
