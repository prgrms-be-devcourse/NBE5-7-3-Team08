package project.backend.domain.chat.chatmessage.dao

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import project.backend.domain.chat.chatmessage.entity.ChatMessageSearch

interface ChatMessageSearchRepository : JpaRepository<ChatMessageSearch, Long> {

    @Query(
        value = """
		SELECT id FROM chat_message_search
		WHERE room_id = :roomId
		AND MATCH(content) AGAINST (:keyword IN NATURAL LANGUAGE MODE)
		ORDER BY id DESC
		LIMIT :limit OFFSET :offset
		""", nativeQuery = true
    )
    fun searchIdsByKeywordAndRoomId(
        @Param("keyword") keyword: String,
        @Param("roomId") roomId: Long,
        @Param("limit") limit: Int,
        @Param("offset") offest: Int
    ): List<Long>

    @Query(
        value = """
            SELECT COUNT(*)
            FROM chat_message_search
            WHERE room_id = :roomId
            AND MATCH(content) AGAINST (:keyword IN NATURAL LANGUAGE MODE)
        """,
        nativeQuery = true
    )
    fun countByKeywordAndRoomId(
        @Param("keyword") keyword: String,
        @Param("roomId") roomId: Long
    ): Long
}