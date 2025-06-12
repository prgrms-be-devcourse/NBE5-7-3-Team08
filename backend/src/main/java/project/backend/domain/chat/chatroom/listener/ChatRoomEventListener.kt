package project.backend.domain.chat.chatroom.listener

import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import project.backend.domain.chat.chatmessage.dao.ChatMessageRepository
import project.backend.domain.chat.chatmessage.entity.ChatMessage
import project.backend.domain.chat.chatmessage.mapper.ChatMessageMapper
import project.backend.domain.chat.chatroom.app.ChatRoomService
import project.backend.domain.chat.chatroom.dao.ChatParticipantRepository
import project.backend.domain.chat.chatroom.dto.event.DeleteChatRoomEvent
import project.backend.domain.chat.chatmessage.dto.event.EventMessageResponse
import project.backend.domain.chat.chatroom.dto.event.JoinChatRoomEvent
import project.backend.domain.chat.chatroom.dto.event.LeaveChatRoomEvent
import project.backend.domain.chat.chatroom.entity.ChatRoom
import project.backend.domain.chat.chatroom.mapper.ChatRoomMapper
import project.backend.domain.member.app.MemberService
import project.backend.domain.member.dto.event.ProfileUpdateEvent
import project.backend.domain.member.entity.Member

@Component
class ChatRoomEventListener(
    private val simpMessagingTemplate: SimpMessagingTemplate,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val chatRoomService: ChatRoomService,
    private val chatMessageMapper: ChatMessageMapper,
    private val memberService: MemberService
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleMemberJoin(joinEvent: JoinChatRoomEvent) {
        val chatRoom: ChatRoom = chatRoomService.getRoomById(joinEvent.roomId)
        val member: Member = memberService.getMemberById(joinEvent.memberId)

        val message: ChatMessage = chatMessageMapper.toEntityWithJoinEvent(chatRoom, member, joinEvent)
        val savedMessage: ChatMessage = chatMessageRepository.save(message)

        val eventMessageResponse: EventMessageResponse =
            ChatRoomMapper.toJoinEventMessageResponse(joinEvent, savedMessage.id!!)

        simpMessagingTemplate.convertAndSend("/topic/chat/${joinEvent.roomId}", eventMessageResponse)
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleProfileUpdate(updateEvent: ProfileUpdateEvent) {
        log.debug("🔥 프로필 업데이트 이벤트 수신: userId={}, nickname={}", updateEvent.userId, updateEvent.nickname)
        simpMessagingTemplate.convertAndSend("/topic/profile-update", updateEvent)
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleMemberLeave(leaveEvent: LeaveChatRoomEvent) {
        val chatRoom = chatRoomService.getRoomById(leaveEvent.roomId)
        val member = memberService.getMemberById(leaveEvent.memberId)

        val message = chatMessageMapper.toEntityWithLeaveEvent(chatRoom, member, leaveEvent)
        val savedMessage = chatMessageRepository.save(message)

        val eventMessageResponse =
            ChatRoomMapper.toLeaveEventMessageResponse(leaveEvent, savedMessage.id!!)

        simpMessagingTemplate.convertAndSend("/topic/chat/${leaveEvent.roomId}", eventMessageResponse)
        simpMessagingTemplate.convertAndSend("/topic/chat/${leaveEvent.roomId}/refresh", leaveEvent.roomId)
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleRoomDelete(deleteEvent: DeleteChatRoomEvent) {
        val eventMessageResponse =
            ChatRoomMapper.toDeleteEventMessageResponse(deleteEvent)

        simpMessagingTemplate.convertAndSend("/topic/chat/${deleteEvent.roomId}/deleted", eventMessageResponse)
    }
}
