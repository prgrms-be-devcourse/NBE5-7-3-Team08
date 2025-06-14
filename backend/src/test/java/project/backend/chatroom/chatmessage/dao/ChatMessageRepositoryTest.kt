package project.backend.domain.chat.chatmessage.dao

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.domain.PageRequest
import project.backend.domain.chat.chatmessage.entity.ChatMessage
import project.backend.domain.chat.chatmessage.entity.MessageStatus
import project.backend.domain.chat.chatmessage.entity.MessageType
import project.backend.domain.chat.chatroom.entity.ChatRoom
import project.backend.domain.member.entity.Member
import project.backend.domain.member.entity.ProviderType
import java.time.LocalDateTime

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("ChatMessageRepository 테스트")
class ChatMessageRepositoryTest {

    @Autowired
    private lateinit var chatMessageRepository: ChatMessageRepository

    @Autowired
    private lateinit var entityManager: TestEntityManager

    private lateinit var testMember1: Member
    private lateinit var testMember2: Member
    private lateinit var testChatRoom1: ChatRoom
    private lateinit var testChatRoom2: ChatRoom
    private lateinit var testMessage1: ChatMessage
    private lateinit var testMessage2: ChatMessage
    private lateinit var testMessage3: ChatMessage

    @BeforeEach
    fun setUp() {
        // 테스트용 멤버 생성
        testMember1 = Member(
            username = "임창인1",
            nickname = "임1",
            email = "test1@example.com",
            provider = ProviderType.LOCAL,
            profileImage = "profile1.jpg"
        )
        testMember2 = Member(
            username = "임창인2",
            nickname = "임2",
            email = "test2@example.com",
            provider = ProviderType.LOCAL,
            profileImage = "profile2.jpg"
        )

        testChatRoom1 = ChatRoom(
            name = "테스트 채팅방 1",
            repositoryUrl = "https://github.com/test/repo1",
            inviteCode = "dadfasfd"
        )
        testChatRoom2 = ChatRoom(
            name = "테스트 채팅방 2",
            repositoryUrl = "https://github.com/test/repo2",
            inviteCode = "asdfasdfa"
        )


        entityManager.persistAndFlush(testMember1)
        entityManager.persistAndFlush(testMember2)
        entityManager.persistAndFlush(testChatRoom1)
        entityManager.persistAndFlush(testChatRoom2)

        testMessage1 = ChatMessage(
            sender = testMember1,
            chatRoom = testChatRoom1,
            content = "첫 번째 메시지",
            sendAt = LocalDateTime.now().minusMinutes(3),
            type = MessageType.TEXT,
            status = MessageStatus.NO_CHANGE
        )
        testMessage2 = ChatMessage(
            sender = testMember2,
            chatRoom = testChatRoom1,
            content = "두 번째 메시지",
            sendAt = LocalDateTime.now().minusMinutes(2),
            type = MessageType.TEXT,
            status = MessageStatus.NO_CHANGE
        )
        testMessage3 = ChatMessage(
            sender = testMember1,
            chatRoom = testChatRoom2,
            content = "세 번째 메시지",
            sendAt = LocalDateTime.now().minusMinutes(1),
            type = MessageType.CODE,
            codeLanguage = "kotlin",
            status = MessageStatus.NO_CHANGE
        )

        entityManager.persistAndFlush(testMessage1)
        entityManager.persistAndFlush(testMessage2)
        entityManager.persistAndFlush(testMessage3)
        entityManager.clear()
    }

    @Test
    @DisplayName("findByIdIn - 존재하는 ID 리스트로 조회")
    fun testFindByIdIn_WithExistingIds() {
        // given
        val ids = listOf(testMessage1.id!!, testMessage2.id!!)

        // when
        val result = chatMessageRepository.findByIdIn(ids)

        // then
        assertThat(result).hasSize(2)
        assertThat(result.map { it.id }).containsExactlyInAnyOrder(testMessage1.id, testMessage2.id)
        assertThat(result.map { it.content }).containsExactlyInAnyOrder("첫 번째 메시지", "두 번째 메시지")
    }



    @Test
    @DisplayName("findByChatRoomIdOrderByIdDesc - 특정 채팅방의 메시지를 ID 내림차순으로 조회")
    fun testFindByChatRoomIdOrderByIdDesc_Success() {
        // given
        val roomId = testChatRoom1.id!!
        val pageable = PageRequest.of(0, 10)

        // when
        val result = chatMessageRepository.findByChatRoomIdOrderByIdDesc(roomId, pageable)

        // then
        assertThat(result).hasSize(2)
        // ID 내림차순으로 정렬되어 있는지 확인
        assertThat(result[0].id).isGreaterThan(result[1].id)
        assertThat(result.map { it.content }).containsExactly("두 번째 메시지", "첫 번째 메시지")
    }

    @Test
    @DisplayName("findByChatRoomIdOrderByIdDesc - 메시지가 없는 채팅방 조회")
    fun testFindByChatRoomIdOrderByIdDesc_EmptyRoom() {
        // given
        val emptyRoom = ChatRoom(
            name = "빈 채팅방",
            repositoryUrl = "https://github.com/test/empty",
            inviteCode = "EMPTY123"
        )
        entityManager.persistAndFlush(emptyRoom)
        val pageable = PageRequest.of(0, 10)

        // when
        val result = chatMessageRepository.findByChatRoomIdOrderByIdDesc(emptyRoom.id!!, pageable)

        // then
        assertThat(result).isEmpty()
    }


    @Test
    @DisplayName("findByChatRoomIdAndIdLessThanOrderByIdDesc - 커서보다 작은 ID의 메시지들 조회")
    fun testFindByChatRoomIdAndIdLessThanOrderByIdDesc_Success() {
        // given
        val roomId = testChatRoom1.id!!
        val cursor = testMessage2.id!! // 두 번째 메시지의 ID를 커서로 사용
        val pageable = PageRequest.of(0, 10)

        // when
        val result = chatMessageRepository.findByChatRoomIdAndIdLessThanOrderByIdDesc(roomId, cursor, pageable)

        // then
        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo(testMessage1.id)
        assertThat(result[0].content).isEqualTo("첫 번째 메시지")
    }

    @Test
    @DisplayName("findByChatRoomIdAndIdLessThanOrderByIdDesc - 커서보다 작은 메시지가 없는 경우")
    fun testFindByChatRoomIdAndIdLessThanOrderByIdDesc_NoMessagesBeforeCursor() {
        // given
        val roomId = testChatRoom1.id!!
        val cursor = testMessage1.id!! // 가장 오래된 메시지의 ID를 커서로 사용
        val pageable = PageRequest.of(0, 10)

        // when
        val result = chatMessageRepository.findByChatRoomIdAndIdLessThanOrderByIdDesc(roomId, cursor, pageable)

        // then
        assertThat(result).isEmpty()
    }



    @Test
    @DisplayName("다른 채팅방의 메시지는 조회되지 않는지 확인")
    fun testChatRoomIsolation() {
        // given
        val room1Id = testChatRoom1.id!!
        val room2Id = testChatRoom2.id!!
        val pageable = PageRequest.of(0, 10)

        // when
        val room1Messages = chatMessageRepository.findByChatRoomIdOrderByIdDesc(room1Id, pageable)
        val room2Messages = chatMessageRepository.findByChatRoomIdOrderByIdDesc(room2Id, pageable)

        // then
        assertThat(room1Messages).hasSize(2)
        assertThat(room2Messages).hasSize(1)
        assertThat(room1Messages.map { it.content }).containsExactlyInAnyOrder("첫 번째 메시지", "두 번째 메시지")
        assertThat(room2Messages[0].content).isEqualTo("세 번째 메시지")
    }
}