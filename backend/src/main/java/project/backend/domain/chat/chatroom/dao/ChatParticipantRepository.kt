package project.backend.domain.chat.chatroom.dao

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import project.backend.domain.chat.chatroom.entity.ChatParticipant
import project.backend.domain.chat.chatroom.entity.ChatRoom

interface ChatParticipantRepository : JpaRepository<ChatParticipant, Long> {

    @EntityGraph(attributePaths = ["participant"])
    @Query(
        """
        SELECT cp 
        FROM ChatParticipant cp 
        WHERE cp.chatRoom = :chatRoom 
          AND cp.isActive = true
        """
    )
    fun findByChatRoom(@Param("chatRoom") chatRoom: ChatRoom): List<ChatParticipant>

    fun findByChatRoomIdAndParticipantIdAndIsActiveTrue(
        chatRoomId: Long,
        participantId: Long
    ): ChatParticipant?

    fun findByChatRoomIdAndParticipantId(
        chatRoomId: Long,
        participantId: Long
    ): ChatParticipant?

    fun findTopByParticipantIdAndIsActiveTrueOrderByJoinAtDesc(
        participantId: Long
    ): ChatParticipant?

    fun findByChatRoomIdAndIsOwnerTrue(
        roomId: Long
    ): ChatParticipant?

    fun existsByParticipantIdAndChatRoomIdAndIsActiveTrue(
        participantId: Long,
        chatRoomId: Long
    ): Boolean
}
