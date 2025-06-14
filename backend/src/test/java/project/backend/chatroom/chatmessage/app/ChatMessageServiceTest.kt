package project.backend.domain.chat.chatmessage.app

import ChatMessageResponse
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import project.backend.domain.chat.chatmessage.dao.ChatMessageRepository
import project.backend.domain.chat.chatmessage.dao.ChatMessageSearchRepository
import project.backend.domain.chat.chatmessage.dto.*
import project.backend.domain.chat.chatmessage.entity.ChatMessage
import project.backend.domain.chat.chatmessage.entity.MessageStatus
import project.backend.domain.chat.chatmessage.entity.MessageType
import project.backend.domain.chat.chatmessage.mapper.ChatMessageMapper
import project.backend.domain.chat.chatroom.app.ChatRoomService
import project.backend.domain.chat.chatroom.entity.ChatRoom
import project.backend.domain.imagefile.ImageFile
import project.backend.domain.imagefile.ImageFileService
import project.backend.domain.member.app.MemberService
import project.backend.domain.member.entity.Member
import project.backend.domain.member.entity.ProviderType
import project.backend.domain.chat.chatmessage.entity.ChatMessageSearch
import project.backend.global.exception.ex.AuthException
import project.backend.global.exception.ex.ChatMessageException
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ChatMessageServiceTest {

    private val chatMessageRepository = mockk<ChatMessageRepository>()
    private val chatRoomService = mockk<ChatRoomService>()
    private val memberService = mockk<MemberService>()
    private val imageFileService = mockk<ImageFileService>()
    private val chatMessageSearchRepository = mockk<ChatMessageSearchRepository>()
    private val messageMapper = mockk<ChatMessageMapper>()

    private lateinit var chatMessageService: ChatMessageService

    private lateinit var testMember: Member
    private lateinit var testRoom: ChatRoom
    private lateinit var testMessage: ChatMessage

    @BeforeEach
    fun setup() {
        chatMessageService = ChatMessageService(
            chatMessageRepository,
            chatRoomService,
            memberService,
            imageFileService,
            chatMessageSearchRepository,
            messageMapper
        )

        testMember = Member(
            id = 1L,
            username = "testuser",
            nickname = "테스트유저",
            provider = ProviderType.LOCAL,
            profileImage = "profile.jpg"
        )

        testRoom = ChatRoom(
            id = 1L,
            name = "테스트룸",
            repositoryUrl = "https://github.com/test/repo",
            inviteCode = "invite123"
        )

        testMessage = ChatMessage(
            id = 1L,
            sender = testMember,
            chatRoom = testRoom,
            content = "테스트 메시지",
            sendAt = LocalDateTime.now(),
            type = MessageType.TEXT,
            status = MessageStatus.NO_CHANGE
        )
    }

    @Test
    fun `텍스트_메시지_저장_성공`() {
        // Given
        val request = ChatMessageRequest(
            content = "테스트 메시지",
            type = MessageType.TEXT
        )
        val response = ChatMessageResponse(
            content = "테스트 메시지",
            senderName = "테스트유저",
            sendAt = LocalDateTime.now(),
            type = MessageType.TEXT,
            senderId = 1L,
            messageId = 1L,
            status = MessageStatus.NO_CHANGE
        )
        val searchEntity = mockk<ChatMessageSearch>()

        every { memberService.getMemberByUsername("testuser") } returns testMember
        every { chatRoomService.getRoomById(1L) } returns testRoom
        every { chatRoomService.validateNotParticipant(1L, 1L) } just Runs
        every { messageMapper.toEntityWithText(testRoom, testMember, request) } returns testMessage
        every { chatMessageRepository.save(testMessage) } returns testMessage
        every { messageMapper.toSearchEntity(testMessage) } returns searchEntity
        every { chatMessageSearchRepository.save(searchEntity) } returns searchEntity
        every { messageMapper.toResponse(testMessage) } returns response

        // When
        val result = chatMessageService.save(1L, request, "testuser")

        // Then
        assertNotNull(result)
        assertEquals("테스트 메시지", result.content)
        assertEquals(MessageType.TEXT, result.type)
        verify { chatMessageRepository.save(testMessage) }
        verify { chatMessageSearchRepository.save(searchEntity) }
    }

    @Test
    fun `이미지_메시지_저장_성공`() {
        // Given
        val imageFile = ImageFile(1L, "stored.jpg", "original.jpg")
        val request = ChatMessageRequest(
            type = MessageType.IMAGE,
            imageFileId = 1L
        )
        val imageMessage = ChatMessage(
            id = 1L,
            sender = testMember,
            chatRoom = testRoom,
            sendAt = LocalDateTime.now(),
            type = MessageType.IMAGE,
            chatImage = imageFile
        )
        val response = ChatMessageResponse(
            senderName = "테스트유저",
            sendAt = LocalDateTime.now(),
            type = MessageType.IMAGE,
            chatImageUrl = "stored.jpg",
            senderId = 1L,
            messageId = 1L,
            status = MessageStatus.NO_CHANGE
        )

        every { memberService.getMemberByUsername("testuser") } returns testMember
        every { chatRoomService.getRoomById(1L) } returns testRoom
        every { chatRoomService.validateNotParticipant(1L, 1L) } just Runs
        every { imageFileService.getImageById(1L) } returns imageFile
        every { messageMapper.toEntityWithImage(testRoom, testMember, imageFile) } returns imageMessage
        every { chatMessageRepository.save(imageMessage) } returns imageMessage
        every { messageMapper.toResponse(imageMessage) } returns response

        // When
        val result = chatMessageService.save(1L, request, "testuser")

        // Then
        assertNotNull(result)
        assertEquals(MessageType.IMAGE, result.type)
        assertEquals("stored.jpg", result.chatImageUrl)
        verify { imageFileService.getImageById(1L) }
    }

    @Test
    fun `잘못된_메시지_타입으로_저장_시도_실패`() {
        // Given
        val request = ChatMessageRequest(
            type = MessageType.GIT
        )

        every { memberService.getMemberByUsername("testuser") } returns testMember
        every { chatRoomService.getRoomById(1L) } returns testRoom
        every { chatRoomService.validateNotParticipant(1L, 1L) } just Runs

        // When & Then
        assertThrows<ChatMessageException> {
            chatMessageService.save(1L, request, "testuser")
        }
    }

    @Test
    fun `메시지_수정_성공`() {
        // Given
        val editRequest = ChatMessageEditRequest(
            messageId = 1L,
            content = "수정된 메시지",
            type = MessageType.TEXT,
            language = null
        )
        val editedMessage = testMessage.apply {
            updateContent("수정된 메시지")
        }
        val searchEntity = mockk<ChatMessageSearch>(relaxed = true)
        val response = ChatMessageResponse(
            content = "수정된 메시지",
            senderName = "테스트유저",
            sendAt = LocalDateTime.now(),
            type = MessageType.TEXT,
            senderId = 1L,
            messageId = 1L,
            status = MessageStatus.EDITED
        )

        every { memberService.getMemberByUsername("testuser") } returns testMember
        every { chatRoomService.getRoomById(1L) } returns testRoom
        every { chatMessageRepository.findById(1L) } returns Optional.of(testMessage)
        every { chatMessageSearchRepository.findById(1L) } returns Optional.of(searchEntity)
        every { messageMapper.toResponse(editedMessage) } returns response

        // When
        val result = chatMessageService.editMessage(1L, editRequest, "testuser")

        // Then
        assertNotNull(result)
        assertEquals("수정된 메시지", result.content)
        assertEquals(MessageStatus.EDITED, result.status)
        verify { searchEntity.updateContent("수정된 메시지") }
    }

    @Test
    fun `다른_사용자의_메시지_수정_시도_실패`() {
        // Given
        val otherUser = Member(
            id = 2L,
            username = "otheruser",
            nickname = "다른유저",
            provider = ProviderType.LOCAL,
            profileImage = "other.jpg"
        )
        val editRequest = ChatMessageEditRequest(
            messageId = 1L,
            content = "수정된 메시지",
            type = MessageType.TEXT,
            language = null
        )

        every { memberService.getMemberByUsername("otheruser") } returns otherUser
        every { chatRoomService.getRoomById(1L) } returns testRoom
        every { chatMessageRepository.findById(1L) } returns Optional.of(testMessage)

        // When & Then
        assertThrows<AuthException> {
            chatMessageService.editMessage(1L, editRequest, "otheruser")
        }
    }

    @Test
    fun `메시지_삭제_성공`() {
        // Given
        val deletedMessage = testMessage.apply { delete() }
        val searchEntity = mockk<ChatMessageSearch>(relaxed = true)
        val response = ChatMessageResponse(
            content = "테스트 메시지",
            senderName = "테스트유저",
            sendAt = LocalDateTime.now(),
            type = MessageType.TEXT,
            senderId = 1L,
            messageId = 1L,
            status = MessageStatus.DELETED
        )

        every { memberService.getMemberByUsername("testuser") } returns testMember
        every { chatRoomService.getRoomById(1L) } returns testRoom
        every { chatMessageRepository.findById(1L) } returns Optional.of(testMessage)
        every { chatMessageSearchRepository.findById(1L) } returns Optional.of(searchEntity)
        every { messageMapper.toResponse(deletedMessage) } returns response

        // When
        val result = chatMessageService.deleteMessage(1L, 1L, "testuser")

        // Then
        assertNotNull(result)
        assertEquals(MessageStatus.DELETED, result.status)
        verify { searchEntity.deleteContent() }
    }


    @Test
    fun `메시지_검색_성공`() {
        // Given
        val searchRequest = ChatMessageSearchRequest("테스트", 0, 10)
        val messageIds = listOf(1L, 2L)
        val message2 = ChatMessage(
            id = 2L,
            sender = testMember,
            chatRoom = testRoom,
            content = "테스트 메시지2",
            sendAt = LocalDateTime.now(),
            type = MessageType.TEXT,
            status = MessageStatus.NO_CHANGE
        )
        val messages = listOf(testMessage, message2)
        val searchResponses = listOf(
            ChatMessageSearchResponse(1L, "테스트 메시지", MessageType.TEXT, "테스트유저", null, LocalDateTime.now()),
            ChatMessageSearchResponse(2L, "테스트 메시지2", MessageType.TEXT, "테스트유저", null, LocalDateTime.now())
        )

        every { chatRoomService.validateNotParticipant(1L, 1L) } just Runs
        every { chatMessageSearchRepository.searchIdsByKeywordAndRoomId("테스트", 1L, 10, 0) } returns messageIds
        every { chatMessageSearchRepository.countByKeywordAndRoomId("테스트", 1L) } returns 2L
        every { chatMessageRepository.findByIdIn(messageIds) } returns messages
        every { messageMapper.toSearchResponse(messages[0]) } returns searchResponses[0]
        every { messageMapper.toSearchResponse(messages[1]) } returns searchResponses[1]

        // When
        val result = chatMessageService.searchMessages(1L, 1L, searchRequest)

        // Then
        assertNotNull(result)
        assertEquals(2, result.content.size)
        assertEquals(2L, result.totalElements)
    }

    @Test
    fun `커서_기반_메시지_조회_성공`() {
        // Given
        val message2 = ChatMessage(
            id = 2L,
            sender = testMember,
            chatRoom = testRoom,
            content = "테스트 메시지2",
            sendAt = LocalDateTime.now(),
            type = MessageType.TEXT,
            status = MessageStatus.NO_CHANGE
        )
        val messages = listOf(testMessage, message2)
        val responses = listOf(
            ChatMessageResponse("테스트 메시지", "테스트유저", LocalDateTime.now(), MessageType.TEXT, null, null, null, 1L, 1L, MessageStatus.NO_CHANGE),
            ChatMessageResponse("테스트 메시지2", "테스트유저", LocalDateTime.now(), MessageType.TEXT, null, null, null, 1L, 2L, MessageStatus.NO_CHANGE)
        )
        val pageRequest = PageRequest.of(0, 3) // size + 1

        every { chatRoomService.getRoomById(1L) } returns testRoom
        every { chatRoomService.validateNotParticipant(1L, 1L) } just Runs
        every { chatMessageRepository.findByChatRoomIdOrderByIdDesc(1L, pageRequest) } returns messages
        every { messageMapper.toResponse(messages[0]) } returns responses[0]
        every { messageMapper.toResponse(messages[1]) } returns responses[1]

        // When
        val result = chatMessageService.getMessagesByRoomId(1L, 1L, null, 2)

        // Then
        assertNotNull(result)
        assertEquals(2, result.messages.size)
        assertEquals(null, result.nextCursor)
    }
}