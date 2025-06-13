package project.backend.chatroom.app

import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.json.JsonWriter
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher
import project.backend.chatroom.util.createChatRoom
import project.backend.chatroom.util.createMember
import project.backend.domain.chat.chatroom.app.ChatRoomService
import project.backend.domain.chat.chatroom.dao.ChatParticipantRepository
import project.backend.domain.chat.chatroom.dao.ChatRoomRepository
import project.backend.domain.chat.chatroom.dto.ChatRoomRequest
import project.backend.domain.chat.chatroom.dto.ChatRoomSimpleResponse
import project.backend.domain.chat.chatroom.dto.event.JoinChatRoomEvent
import project.backend.domain.chat.chatroom.dto.event.LeaveChatRoomEvent
import project.backend.domain.chat.chatroom.entity.ChatParticipant
import project.backend.domain.chat.chatroom.entity.ChatRoom
import project.backend.domain.chat.chatroom.mapper.ChatRoomMapper
import project.backend.domain.chat.github.app.GitMessageService
import project.backend.domain.member.app.MemberService
import project.backend.domain.member.entity.Member
import project.backend.domain.member.entity.ProviderType
import project.backend.global.exception.ex.ChatRoomException
import java.time.LocalDateTime
import kotlin.test.Test

class ChatRoomServiceTests {

    val chatRoomRepository = mockk<ChatRoomRepository>()
    val chatParticipantRepository = mockk<ChatParticipantRepository>()
    val chatRoomMapper = mockk<ChatRoomMapper>()
    val memberService = mockk<MemberService>()
    val gitMessageService = mockk<GitMessageService>()
    val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

    val service = ChatRoomService(
        chatRoomRepository,
        chatParticipantRepository,
        chatRoomMapper,
        memberService,
        gitMessageService,
        eventPublisher,
        githubUsername = "GitHubBot"
    )

    @Test
    fun `채팅방 생성 시 참여자 등록 및 깃허브 봇 참가`() {
        // given
        val member = Member(
            id = 1L,
            username = "userA",
            nickname = "User",
            provider = ProviderType.LOCAL,
            profileImage = "img.png"
        )
        val request = ChatRoomRequest("채팅방", "https://github.com/repo")

        val chatRoom = mockk<ChatRoom>(relaxed = true)
        every { chatRoom.id } returns 10L
        every { chatRoom.name } returns "채팅방"
        every { chatRoom.inviteCode } returns "INVITE123"

        every { memberService.getMemberById(1L) } returns member
        every { memberService.getMemberByUsername("GitHubBot") } returns mockk(relaxed = true) // 수정
        every { chatRoomMapper.toEntity(request) } returns chatRoom
        every { chatRoomMapper.toSimpleResponse(any(), any()) } returns ChatRoomSimpleResponse(
            10L, "채팅방", "https://github.com/repo", member.id!!, "INVITE123"
        )
        every { chatRoomRepository.save(chatRoom) } returns chatRoom
        every {
            gitMessageService.registerWebhook(
                "https://github.com/repo",
                10L,
                1L
            )
        } returns Unit // 추가

        // when
        val response = service.createChatRoom(request, 1L)

        // then
        assertThat(response.name).isEqualTo("채팅방")
        verify { gitMessageService.registerWebhook("https://github.com/repo", 10L, 1L) }
    }


    @Test
    fun `초대코드로 채팅방 참여 성공 테스트`() {
        // given
        val inviteCode = "JOIN777"
        val memberId = 1L

        val chatRoom = createChatRoom(
            name = "조인할 채팅방",
            repositoryUrl = "https://github.com/repo",
            id = 1L,
            inviteCode = inviteCode
        )
        val member = createMember(id = 1L, username = "User", profileImage = "img.png")

        every { chatRoomRepository.findByInviteCode(inviteCode) } returns chatRoom
        every { memberService.getMemberById(memberId) } returns member
        every { chatParticipantRepository.save(any()) } answers { firstArg() }
        every {
            chatParticipantRepository.findByChatRoomIdAndParticipantId(
                any(),
                any()
            )
        } returns null
        every { eventPublisher.publishEvent(any()) } just Runs

        // when
        val response = service.joinChatRoom(inviteCode, memberId)

        // then
        assertThat(response.inviteCode).isEqualTo(inviteCode)
    }

    @Test
    fun `잘못된 초대코드로 채팅방 참여시 실패`() {
        // given
        val invalidCode = "WRONG_CODE"
        val memberId = 1L

        every { chatRoomRepository.findByInviteCode(invalidCode) } returns null

        // when & then
        assertThrows<ChatRoomException> {
            service.joinChatRoom(invalidCode, memberId)
        }
    }

    @Test
    fun `채팅방에서 정상적으로 퇴장하면 이벤트가 발행되고 최근방이 갱신된다`() {
        // given
        val roomId = 1L
        val memberId = 100L

        val participant = mockk<ChatParticipant>(relaxed = true)
        every { participant.owner } returns false
        every { participant.leave() } just Runs

        every {
            chatParticipantRepository.findByChatRoomIdAndParticipantIdAndIsActiveTrue(
                roomId, memberId
            )
        } returns participant

        val member = mockk<Member>(relaxed = true)
        every { member.nickname } returns "test"
        every { memberService.getMemberById(memberId) } returns member
        every {
            chatParticipantRepository.findTopByParticipantIdAndIsActiveTrueOrderByJoinAtDesc(
                memberId
            )
        } returns null

        every { eventPublisher.publishEvent(any()) } just Runs

        // when
        service.leaveChatRoom(roomId, memberId)

        // then
        verify { participant.leave() }
        verify {
            eventPublisher.publishEvent(
                match<LeaveChatRoomEvent> {
                    it.roomId == roomId &&
                            it.memberId == memberId &&
                            it.nickname == "test"
                }
            )
        }
    }



}
