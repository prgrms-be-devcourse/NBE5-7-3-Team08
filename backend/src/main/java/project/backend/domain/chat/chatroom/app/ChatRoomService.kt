package project.backend.domain.chat.chatroom.app

import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import project.backend.domain.chat.chatroom.dao.ChatParticipantRepository
import project.backend.domain.chat.chatroom.dao.ChatRoomRepository
import project.backend.domain.chat.chatroom.dto.*
import project.backend.domain.chat.chatroom.dto.event.DeleteChatRoomEvent
import project.backend.domain.chat.chatroom.dto.event.JoinChatRoomEvent
import project.backend.domain.chat.chatroom.dto.event.LeaveChatRoomEvent
import project.backend.domain.chat.chatroom.entity.ChatParticipant
import project.backend.domain.chat.chatroom.entity.ChatRoom
import project.backend.domain.chat.chatroom.mapper.ChatRoomMapper
import project.backend.domain.chat.github.app.GitMessageService
import project.backend.domain.member.app.MemberService
import project.backend.domain.member.entity.Member
import project.backend.global.exception.errorcode.ChatRoomErrorCode
import project.backend.global.exception.ex.ChatRoomException
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Collectors


@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class ChatRoomService @Autowired constructor(
    val chatRoomRepository: ChatRoomRepository,
    val chatParticipantRepository: ChatParticipantRepository,
    val chatRoomMapper: ChatRoomMapper,
    val memberService: MemberService,
    val gitMessageService: GitMessageService,
    val eventPublisher: ApplicationEventPublisher
) {
    @Value("\${github.username}")
    private val githubUsername: String? = null

    @Transactional
    fun createChatRoom(request: ChatRoomRequest, ownerId: Long?): ChatRoomSimpleResponse {
        val owner = memberService.getMemberById(ownerId)

        val chatRoom = chatRoomMapper.toEntity(request)

        val chatParticipant = ChatParticipant.createOwner(owner, chatRoom)
        chatRoom.addParticipant(chatParticipant)

        val savedRoom = chatRoomRepository.save(chatRoom)

        if (!request.repositoryUrl.isBlank()) {
            gitMessageService.registerWebhook(
                request.repositoryUrl,
                savedRoom.id!!, owner.id!!
            ) //웹훅 자동 등록
            joinGitHubBot(savedRoom) //깃허브봇 채팅 참가
        }

        return chatRoomMapper.toSimpleResponse(savedRoom, owner)
    }

    private fun joinGitHubBot(room: ChatRoom) {
        val githubBot = memberService.getMemberByUsername(githubUsername)
        val gitParticipant = ChatParticipant.of(githubBot, room)
        room.addParticipant(gitParticipant)
    }

    @Transactional(readOnly = true)
    fun getInviteCode(roomId: Long): String {
        val room = getRoomById(roomId)

        return room.inviteCode
    }

    @Transactional(readOnly = true)
    fun getRoomIdByInviteCode(inviteCode: String): Long? {
        val room = getByInviteCode(inviteCode)

        return room.id
    }

    @Transactional
    fun joinChatRoom(inviteCode: String, memberId: Long?): InviteJoinResponse {
        val room = getByInviteCode(inviteCode)
        val member = memberService.getMemberById(memberId)

        handleParticipantJoin(room, member)

        eventPublisher.publishEvent(
            JoinChatRoomEvent(
                room.id!!, memberId!!, member.nickname,
                LocalDateTime.now()
            )
        )

        return ChatRoomMapper.toInviteJoinResponse(
            room.id!!, room.inviteCode,
            room.name
        )
    }

    private fun handleParticipantJoin(room: ChatRoom, member: Member) {
        //참여중 여부와 관계없이 기존 참가 기록들을 확인
        val existingParticipant: ChatParticipant? =
            chatParticipantRepository.findByChatRoomIdAndParticipantId(
                room.id!!, member.id!!
            )

        if (existingParticipant != null) {
            if (existingParticipant.isActive) {
                throw ChatRoomException(ChatRoomErrorCode.ALREADY_PARTICIPANT)
            }
            existingParticipant.rejoin()
        } else {
            val chatParticipant = ChatParticipant.of(member, room)
            room.addParticipant(chatParticipant)
        }
    }


    @Transactional(readOnly = true)
    fun getRecentRoomInviteCode(memberId: Long?): String {
        val roomId = memberService.getMemberById(memberId).recentRoomId
            ?: throw ChatRoomException(ChatRoomErrorCode.CHATROOM_NOT_EXIST)

        // 아무 채팅방에도 참여한 적이 없음 → 예외 던지기

        return getRoomById(roomId).inviteCode
    }

    @Transactional(readOnly = true)
    fun findAllRoomsByOwnerId(memberId: Long, pageable: Pageable): Page<MyChatRoomResponse> {
        val allRoomsByOwnerId = chatRoomRepository.findAllRoomsByOwnerId(
            memberId,
            pageable
        )

        return allRoomsByOwnerId.map(ChatRoomMapper::toProfileResponse)
    }


    @Transactional(readOnly = true)
    fun findChatRoomsByMemberId(memberId: Long, pageable: Pageable): Page<RoomInfoResponse> {
        val chatRooms = chatRoomRepository.findChatRoomsByParticipantId(
            memberId, pageable
        )

        return chatRooms.map(ChatRoomMapper::toListResponse)
    }

    // 채팅방의 참가자 목록 조회
    @Transactional(readOnly = true)
    fun getParticipants(memberId: Long, roomId: Long): MutableList<Any>? {
        val chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow { ChatRoomException(ChatRoomErrorCode.CHATROOM_NOT_FOUND) }

        validateNotParticipant(memberId, roomId)

        val participants = chatParticipantRepository.findByChatRoom(chatRoom)

        return participants.stream()
            .map<Any>(ChatRoomMapper::toParticipantResponse).collect(Collectors.toList<Any>())
    }

    //임창인
    @Transactional
    fun leaveChatRoom(roomId: Long, memberId: Long) {
        val participant = chatParticipantRepository
            .findByChatRoomIdAndParticipantIdAndIsActiveTrue(roomId, memberId)
            ?: throw ChatRoomException(ChatRoomErrorCode.NOT_PARTICIPANT)

        if (participant.isOwner) {
            throw ChatRoomException(ChatRoomErrorCode.OWNER_CANNOT_LEAVE)
        }

        participant.leave()

        val member = memberService.getMemberById(memberId)

        eventPublisher.publishEvent(
            LeaveChatRoomEvent(
                roomId = roomId,
                memberId = memberId,
                nickname = member.nickname,
                leaveAt = LocalDateTime.now()
            )
        )
        updateRecentRoomAfterLeaving(memberId)
    }

    private fun updateRecentRoomAfterLeaving(memberId: Long) {
        val member = memberService.getMemberById(memberId)

        val mostRecentActiveRoom = chatParticipantRepository
            .findTopByParticipantIdAndIsActiveTrueOrderByJoinAtDesc(memberId)

        member.recentRoomId = mostRecentActiveRoom?.chatRoom?.id
    }

    private fun getByInviteCode(inviteCode: String): ChatRoom {
        return chatRoomRepository.findByInviteCode(inviteCode)
            ?: throw ChatRoomException(ChatRoomErrorCode.CHATROOM_NOT_FOUND)
    }

    fun getRoomById(roomId: Long): ChatRoom {
        return chatRoomRepository.findById(roomId)
            .orElseThrow { ChatRoomException(ChatRoomErrorCode.CHATROOM_NOT_FOUND) }
    }

    @Transactional
    fun getEntryInfo(inviteCode: String, memberId: Long): EntryRoomResponse {
        val room = getByInviteCode(inviteCode)
        validateNotParticipant(memberId, room.id!!)

        memberService.getMemberById(memberId).recentRoomId = room.id //recentRoomId 업데이트

        val ownerId = findOwnerId(room.id!!)

        return EntryRoomResponse(room.id!!, room.name, ownerId!!)
    }

    private fun findOwnerId(roomId: Long): Long? {
        val owner: ChatParticipant =
            chatParticipantRepository.findByChatRoomIdAndIsOwnerTrue(roomId)
                ?: throw ChatRoomException(ChatRoomErrorCode.OWNER_NOT_FOUND)
        return owner.participant.id
    }

    @Transactional(readOnly = true)
    fun getRoomInfo(inviteCode: String, memberId: Long?): RoomInfoResponse {
        val room = getByInviteCode(inviteCode)
        return ChatRoomMapper.toListResponse(room)
    }

    fun validateNotParticipant(memberId: Long, roomId: Long) {
        if (!chatParticipantRepository.existsByParticipantIdAndChatRoomIdAndIsActiveTrue
                (memberId, roomId)
        ) {
            throw ChatRoomException(ChatRoomErrorCode.NOT_PARTICIPANT)
        }
    }

    @Transactional
    fun deleteChatRoom(roomId: Long, memberId: Long) {
        val room = getRoomById(roomId)

        val participant: ChatParticipant =
            chatParticipantRepository.findByChatRoomIdAndParticipantIdAndIsActiveTrue(
                roomId, memberId
            ) ?: throw ChatRoomException(ChatRoomErrorCode.NOT_PARTICIPANT)

        if (!participant.isOwner) {
            throw ChatRoomException(ChatRoomErrorCode.OWNER_PERMISSION_REQUIRED)
        }

        eventPublisher.publishEvent(
            DeleteChatRoomEvent(roomId, room.name)
        )

        chatRoomRepository.delete(room)
    }
}

