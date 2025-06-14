package project.backend.domain.chat.chatmessage.dao

import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import project.backend.domain.chat.chatmessage.entity.ChatMessageSearch
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChatMessageSearchRepositoryTest {

    private val chatMessageSearchRepository = mockk<ChatMessageSearchRepository>()

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @Test
    fun `키워드로_메시지_검색_성공`() {
        // Given
        val keyword = "테스트"
        val roomId = 1L
        val expectedIds = listOf(1L, 3L, 5L)
        val expectedCount = 3L

        every {
            chatMessageSearchRepository.searchIdsByKeywordAndRoomId(keyword, roomId, 10, 0)
        } returns expectedIds

        every {
            chatMessageSearchRepository.countByKeywordAndRoomId(keyword, roomId)
        } returns expectedCount

        // When
        val searchResult = chatMessageSearchRepository.searchIdsByKeywordAndRoomId(keyword, roomId, 10, 0)
        val countResult = chatMessageSearchRepository.countByKeywordAndRoomId(keyword, roomId)

        // Then
        assertEquals(expectedIds, searchResult)
        assertEquals(expectedCount, countResult)
    }

    @Test
    fun `페이징_처리_테스트`() {
        // Given
        val keyword = "테스트"
        val roomId = 1L
        val firstPageIds = listOf(10L, 9L, 8L, 7L, 6L)
        val secondPageIds = listOf(5L, 4L, 3L, 2L, 1L)

        every {
            chatMessageSearchRepository.searchIdsByKeywordAndRoomId(keyword, roomId, 5, 0)
        } returns firstPageIds

        every {
            chatMessageSearchRepository.searchIdsByKeywordAndRoomId(keyword, roomId, 5, 5)
        } returns secondPageIds

        // When
        val firstPage = chatMessageSearchRepository.searchIdsByKeywordAndRoomId(keyword, roomId, 5, 0)
        val secondPage = chatMessageSearchRepository.searchIdsByKeywordAndRoomId(keyword, roomId, 5, 5)

        // Then
        assertEquals(5, firstPage.size)
        assertEquals(5, secondPage.size)
        assertEquals(10L, firstPage.first())

        val intersection = firstPage.intersect(secondPage.toSet())
        assertTrue(intersection.isEmpty())
    }

    @Test
    fun `채팅방별_검색_격리_테스트`() {
        // Given
        val keyword = "공통키워드"
        val room1Id = 1L
        val room2Id = 2L
        val room1Ids = listOf(1L, 3L, 5L)
        val room2Ids = listOf(2L, 4L, 6L)

        every {
            chatMessageSearchRepository.searchIdsByKeywordAndRoomId(keyword, room1Id, 10, 0)
        } returns room1Ids

        every {
            chatMessageSearchRepository.searchIdsByKeywordAndRoomId(keyword, room2Id, 10, 0)
        } returns room2Ids

        // When
        val room1Result = chatMessageSearchRepository.searchIdsByKeywordAndRoomId(keyword, room1Id, 10, 0)
        val room2Result = chatMessageSearchRepository.searchIdsByKeywordAndRoomId(keyword, room2Id, 10, 0)

        // Then
        assertEquals(room1Ids, room1Result)
        assertEquals(room2Ids, room2Result)

        val intersection = room1Result.intersect(room2Result.toSet())
        assertTrue(intersection.isEmpty())
    }

    @Test
    fun `검색_엔티티_수정_및_삭제_테스트`() {
        // Given
        val messageId = 1L
        val searchEntity = mockk<ChatMessageSearch>(relaxed = true)

        every { chatMessageSearchRepository.findById(messageId) } returns Optional.of(searchEntity)

        // When & Then - 수정
        val foundEntity = chatMessageSearchRepository.findById(messageId)
        foundEntity.ifPresent { it.updateContent("수정된 메시지") }
        verify(exactly = 1) { searchEntity.updateContent("수정된 메시지") }

        // When & Then - 삭제
        foundEntity.ifPresent { it.deleteContent() }
        verify(exactly = 1) { searchEntity.deleteContent() }
    }
}