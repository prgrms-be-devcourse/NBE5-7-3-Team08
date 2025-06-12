package project.backend.domain.chat.chatmessage.dao

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import project.backend.domain.chat.chatmessage.entity.ChatMessage

interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {

    fun findByIdIn(ids: List<Long>): List<ChatMessage>

    fun findByChatRoomIdOrderByIdDesc(roomId: Long, pageable: Pageable): List<ChatMessage>

    fun findByChatRoomIdAndIdLessThanOrderByIdDesc(
        roomId: Long,
        cursor: Long,
        pageable: Pageable
    ): List<ChatMessage>
}