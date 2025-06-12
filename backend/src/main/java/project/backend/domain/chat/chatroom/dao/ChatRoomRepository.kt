package project.backend.domain.chat.chatroom.dao

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import project.backend.domain.chat.chatroom.entity.ChatRoom

interface ChatRoomRepository : JpaRepository<ChatRoom, Long> {

    @Query(
        """
        SELECT DISTINCT cr
        FROM ChatRoom cr
        JOIN cr.participants cp
        WHERE cp.participant.id = :memberId AND cp.isActive = true
        """
    )
    fun findChatRoomsByParticipantId(
        @Param("memberId") memberId: Long,
        pageable: Pageable
    ): Page<ChatRoom>

    fun findByInviteCode(inviteCode: String): ChatRoom?

    @Query(
        """
        SELECT cr
        FROM ChatRoom cr
        JOIN cr.participants cp
        WHERE cp.participant.id = :ownerId AND cp.isOwner=true AND cp.isActive = true
        """
    )
    fun findAllRoomsByOwnerId(
        ownerId: Long,
        pageable: Pageable
    ): Page<ChatRoom>
}
