package project.backend.global.initializer

import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import project.backend.domain.chat.chatroom.dao.ChatRoomRepository
import project.backend.domain.chat.chatroom.entity.ChatParticipant
import project.backend.domain.chat.chatroom.entity.ChatRoom
import project.backend.domain.member.app.MemberService
import project.backend.domain.member.dao.MemberRepository
import project.backend.domain.member.dto.SignUpRequest
import java.util.*
import kotlin.math.min

@Component
@Profile("local")
class MockDataInitializer(
    private val memberService: MemberService,
    private val chatRoomRepository: ChatRoomRepository,
    private val memberRepository: MemberRepository
) {
    @PostConstruct
    fun MockDataGenerator() {
        // 1. 테스트용 멤버 생성
        val emails = listOf(
            "test1@test.com", "test2@test.com", "test3@test.com", "test4@test.com",
            "test5@test.com", "test6@test.com", "test7@test.com"
        )

        emails.forEachIndexed { index, email ->
            val nickname = "test${index + 1}"
            val password = nickname

            // 중복 방지: 이미 존재하면 skip
            if (memberRepository.findByEmail(email).isEmpty) {
                val request = SignUpRequest().apply {
                    this.email = email
                    this.nickname = nickname
                    this.password = password
                }

                memberService.saveMember(request)
            }
        }

        val allMembers = memberRepository.findAll() // 저장된 전체 멤버

        // 2. 각 멤버가 3개씩 채팅방 생성 + 참가자 3명씩 포함
        allMembers.forEachIndexed { i, owner ->
            for (j in 1 until allMembers.size) {
                val room = ChatRoom.builder()
                    .name("TestRoom-${i + 1}-$j")
                    .repositoryUrl("https://github.com/test${i + 1}/repo$j")
                    .inviteCode(UUID.randomUUID().toString())
                    .build()

                // 참가자 최대 3명 포함 (owner 포함 가능)
                for (k in 0 until min(3, allMembers.size)) {
                    val participant = allMembers[k]

                    val chatParticipant = ChatParticipant.builder()
                        .participant(participant)
                        .chatRoom(room)
                        .build()

                    room.participants.add(chatParticipant) // 양방향 연결
                }

                chatRoomRepository.save(room) // cascade 로 참가자도 함께 저장됨
            }
        }
        chatRoomRepository.flush()
    }
}

