package project.backend.domain.chat.chatroom.entity

import jakarta.persistence.*
import lombok.AccessLevel
import lombok.Builder
import lombok.Getter
import lombok.NoArgsConstructor
import project.backend.domain.member.entity.Member
import java.time.LocalDateTime

@Entity
class ChatParticipant protected constructor() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_participant_id")
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    lateinit var participant: Member

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    lateinit var chatRoom: ChatRoom

    var owner: Boolean = false
    var isActive: Boolean = true
    var joinAt: LocalDateTime? = null

    // 생성자 (Builder 대체)
    constructor(
        id: Long? = null,
        participant: Member,
        chatRoom: ChatRoom,
        owner: Boolean = false,
        joinAt: LocalDateTime = LocalDateTime.now()
    ) : this() {
        this.id = id
        this.participant = participant
        this.chatRoom = chatRoom
        this.owner = owner
        this.joinAt = joinAt
    }

    companion object {
        fun of(participant: Member, chatRoom: ChatRoom): ChatParticipant {
            return ChatParticipant(
                participant = participant,
                chatRoom = chatRoom,
                joinAt = LocalDateTime.now()
            )
        }

        fun createOwner(participant: Member, chatRoom: ChatRoom): ChatParticipant {
            return ChatParticipant(
                participant = participant,
                chatRoom = chatRoom,
                owner = true,
                joinAt = LocalDateTime.now()
            )
        }
    }

    fun leave() {
        isActive = false
    }

    fun rejoin() {
        isActive = true
    }
}
