package project.backend.chatroom.dao

import io.github.oshai.kotlinlogging.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import project.backend.chatroom.util.createChatRoom
import project.backend.chatroom.util.createMember
import project.backend.chatroom.util.createParticipant
import project.backend.domain.chat.chatroom.dao.ChatParticipantRepository
import project.backend.domain.chat.chatroom.dao.ChatRoomRepository
import project.backend.domain.member.dao.MemberRepository

private val log = KotlinLogging.logger {}

@SpringBootTest
class ChatRoomRepositoryTests @Autowired constructor(
    val chatRoomRepository: ChatRoomRepository,
    val memberRepository: MemberRepository,
    val participantRepository: ChatParticipantRepository
) {

    @Test
    fun `repository 주입 테스트`() {
        log.info { chatRoomRepository }
        assertThat(chatRoomRepository).isNotNull()
    }

    @Test
    @Transactional
    fun `채팅방 저장 테스트`() {
        // given
        val member = memberRepository.save(createMember(id = 1L, username = "userSave"))

        // when
        val room = chatRoomRepository.save(createChatRoom(id = 2L, name = "참여방"))
        val participant = participantRepository.save(createParticipant(member, room))

        // then
        assertThat(room.name).isEqualTo("참여방")

        // DB로부터 다시 조회해서 participants 연관관계 확인 (JPA 연관관계 주의!)
        val foundRoom = chatRoomRepository.findById(room.id!!).get()

        assertThat(foundRoom.participants).hasSize(1)
        assertThat(foundRoom.participants.first().participant.username).isEqualTo("userSave")
    }

    @Test
    fun `참여자 ID로 채팅방 조회 테스트`() {
        // given
        val member = memberRepository.save(createMember(id = 2L, username = "userRead"))
        val room = chatRoomRepository.save(createChatRoom(id = 2L, name = "참여방"))
        val participant = participantRepository.save(createParticipant(member, room))

        // when
        val page = chatRoomRepository.findChatRoomsByParticipantId(
            memberId = member.id!!,
            pageable = PageRequest.of(0, 5)
        )

        // then
        assertThat(page.content).hasSize(1)
        assertThat(page.content[0].name).isEqualTo("참여방")
    }

    @Test
    fun `초대코드로 채팅방 조회 테스트`() {
        //given
        val inviteCode = "JOIN777"
        val room = chatRoomRepository.save(createChatRoom(name = "채팅방", id=3L,inviteCode = inviteCode))

        //when
        val found = chatRoomRepository.findByInviteCode(inviteCode)

        //ten
        assertThat(found).isNotNull
        assertThat(found!!.inviteCode).isEqualTo(inviteCode)
    }

    @Test
    fun `Owner id로 내가 만든 채팅방 조회 테스트`() {
        //given
        val owner = memberRepository.save(createMember(id= 3L, username = "ownerA"))
        val room1 = chatRoomRepository.save(createChatRoom(id = 3L, name = "Room A"))
        val room2 = chatRoomRepository.save(createChatRoom(id = 4L, name = "Room B"))
        participantRepository.save(createParticipant(owner, room1, owner = true))
        participantRepository.save(createParticipant(owner, room2, owner = true))

        //when
        val result = chatRoomRepository.findAllRoomsByOwnerId(owner.id!!, PageRequest.of(0, 10))

        //then
        assertThat(result.content).hasSize(2)
        assertThat(result.content.map { it.name }).containsExactlyInAnyOrder("Room A", "Room B")
    }
}
