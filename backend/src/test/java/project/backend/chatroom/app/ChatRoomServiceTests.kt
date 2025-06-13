package project.backend.chatroom.app

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher
import project.backend.chatroom.util.createMember
import project.backend.domain.chat.chatroom.app.ChatRoomService
import project.backend.domain.chat.chatroom.dao.ChatParticipantRepository
import project.backend.domain.chat.chatroom.dao.ChatRoomRepository
import project.backend.domain.chat.chatroom.dto.ChatRoomRequest
import project.backend.domain.chat.chatroom.dto.ChatRoomSimpleResponse
import project.backend.domain.chat.chatroom.entity.ChatRoom
import project.backend.domain.chat.chatroom.mapper.ChatRoomMapper
import project.backend.domain.chat.github.app.GitMessageService
import project.backend.domain.member.app.MemberService
import project.backend.domain.member.entity.Member
import project.backend.domain.member.entity.ProviderType
import kotlin.test.Test

class ChatRoomServiceTests {

    val chatRoomRepository = mockk<ChatRoomRepository>()
    val chatParticipantRepository = mockk<ChatParticipantRepository>()
    val chatRoomMapper = mockk<ChatRoomMapper>()
    val memberService = mockk<MemberService>()
    val gitMessageService = mockk<GitMessageService>()
    val eventPublisher = mockk<ApplicationEventPublisher>()

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
        every { gitMessageService.registerWebhook("https://github.com/repo", 10L, 1L) } returns Unit // 추가

        // when
        val response = service.createChatRoom(request, 1L)

        // then
        assertThat(response.name).isEqualTo("채팅방")
        verify { gitMessageService.registerWebhook("https://github.com/repo", 10L, 1L) }
    }



//    @Test
//    fun `채팅방 참여 테스트`() {
//        //given
//        val member = Member(
//            id = 1L,
//
//            )
//    }

}