package project.backend.domain.chat.chatmessage.app

import ChatMessageResponse
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import project.backend.domain.chat.chatmessage.dao.ChatMessageRepository
import project.backend.domain.chat.chatmessage.dao.ChatMessageSearchRepository
import project.backend.domain.chat.chatmessage.dto.*
import project.backend.domain.chat.chatmessage.entity.ChatMessage
import project.backend.domain.chat.chatmessage.entity.MessageType
import project.backend.domain.chat.chatmessage.mapper.ChatMessageMapper
import project.backend.domain.chat.chatroom.app.ChatRoomService
import project.backend.domain.imagefile.ImageFileService
import project.backend.domain.member.app.MemberService
import project.backend.global.exception.errorcode.AuthErrorCode
import project.backend.global.exception.errorcode.ChatMessageErrorCode
import project.backend.global.exception.ex.AuthException
import project.backend.global.exception.ex.ChatMessageException

@Service
class ChatMessageService(
    private val chatMessageRepository: ChatMessageRepository,
    private val chatRoomService: ChatRoomService,
    private val memberService: MemberService,
    private val imageFileService: ImageFileService,
    private val chatMessageSearchRepository: ChatMessageSearchRepository,
    private val messageMapper: ChatMessageMapper
) {

    @Transactional
    fun save(roomId: Long, request: ChatMessageRequest, username: String): ChatMessageResponse {
        val sender = memberService.getMemberByUsername(username)
        val room = chatRoomService.getRoomById(roomId)

        chatRoomService.validateNotParticipant(sender.id, roomId)

        val message = when (request.type) {
            MessageType.IMAGE -> {
                val findImage = imageFileService.getImageById(request.imageFileId!!)
                messageMapper.toEntityWithImage(room, sender, findImage)
            }
            MessageType.TEXT -> messageMapper.toEntityWithText(room, sender, request)
            MessageType.CODE -> messageMapper.toEntityWithCode(room, sender, request)
            else -> throw ChatMessageException(ChatMessageErrorCode.INVALID_ROUTE)
        }

        chatMessageRepository.save(message)

        if (isSearchable(message)) {
            val searchMessage = messageMapper.toSearchEntity(message)
            chatMessageSearchRepository.save(searchMessage)
        }

        return messageMapper.toResponse(message)
    }

    private fun isSearchable(message: ChatMessage): Boolean {
        return message.type != MessageType.IMAGE
    }

    @Transactional(readOnly = true)
    fun searchMessages(
        memberId: Long,
        roomId: Long,
        @Valid request: ChatMessageSearchRequest
    ): Page<ChatMessageSearchResponse> {
        val keyword = request.keyword
        val page = request.page
        val size = request.pageSize
        val offset = page * size

        chatRoomService.validateNotParticipant(memberId, roomId)

        val messageIds = chatMessageSearchRepository.searchIdsByKeywordAndRoomId(
            keyword, roomId, size, offset
        )

        val totalCount = chatMessageSearchRepository.countByKeywordAndRoomId(keyword, roomId)

        val chatMessages = chatMessageRepository.findByIdIn(messageIds)

        val messageMap = chatMessages.associateBy { it.id }

        val resultList = messageIds.mapNotNull { messageMap[it] }
            .map { messageMapper.toSearchResponse(it) }

        return PageImpl(resultList, PageRequest.of(page, size), totalCount)
    }

    @Transactional
    fun editMessage(
        roomId: Long,
        request: ChatMessageEditRequest,
        username: String
    ): ChatMessageResponse {

        memberService.getMemberByUsername(username)
        chatRoomService.getRoomById(roomId)

        val message = chatMessageRepository.findById(request.messageId)
            .orElseThrow { ChatMessageException(ChatMessageErrorCode.MESSAGE_NOT_FOUND) }

        if (message.sender.username != username) {
            throw AuthException(AuthErrorCode.FORBIDDEN_MESSAGE_EDIT)
        }

        message.updateContent(request.content)

        if (message.type == MessageType.CODE) {
            message.updateLanguage(request.language)
        }

        if (isSearchable(message)) {
            chatMessageSearchRepository.findById(message.id)
                .ifPresent { searchEntity ->
                    searchEntity.updateContent(message.content ?: "")
                }
        }

        return messageMapper.toResponse(message)
    }

    @Transactional
    fun deleteMessage(roomId: Long, messageId: Long, username: String): ChatMessageResponse {

        memberService.getMemberByUsername(username)
        chatRoomService.getRoomById(roomId)

        val message = chatMessageRepository.findById(messageId)
            .orElseThrow { ChatMessageException(ChatMessageErrorCode.MESSAGE_NOT_FOUND) }

        if (message.sender.username != username) {
            throw AuthException(AuthErrorCode.FORBIDDEN_MESSAGE_DELETE)
        }

        message.delete()

        if (isSearchable(message)) {
            chatMessageSearchRepository.findById(message.id)
                .ifPresent { it.deleteContent() }
        }

        return messageMapper.toResponse(message)
    }

    @Transactional(readOnly = true)
    fun getMessagesByRoomId(
        memberId: Long,
        roomId: Long,
        cursor: Long?,
        size: Int
    ): ChatScrollResponse {
        chatRoomService.getRoomById(roomId)
        chatRoomService.validateNotParticipant(memberId, roomId)

        val pageRequest = PageRequest.of(0, size + 1)

        val result = if (cursor == null) {
            chatMessageRepository.findByChatRoomIdOrderByIdDesc(roomId, pageRequest)
        } else {
            chatMessageRepository.findByChatRoomIdAndIdLessThanOrderByIdDesc(
                roomId, cursor, pageRequest
            )
        }

        val scroll = ScrollPaginationCollection.of(result, size)

        val responses = scroll.getCurrentScrollItems()
            .map { messageMapper.toResponse(it) }

        val nextCursor = if (scroll.isLastScroll()) null else scroll.getNextCursor()?.id

        return ChatScrollResponse(responses, nextCursor)
    }
}