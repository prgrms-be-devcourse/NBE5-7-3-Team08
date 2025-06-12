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
    fun mockDataGenerator() {
        val emails = listOf(
            "test1@test.com", "test2@test.com", "test3@test.com", "test4@test.com",
            "test5@test.com", "test6@test.com", "test7@test.com"
        )

        emails.forEachIndexed { index, email ->
            if (memberRepository.findByEmail(email) == null) {
                memberService.saveMember(
                    SignUpRequest(
                        username = "test${index + 1}",
                        nickname = "test${index + 1}",
                        email = email,
                        password = "test${index + 1}"
                    )
                )
            }
        }

        val allMembers = memberRepository.findAll()

        allMembers.forEachIndexed { i, _ ->
            (1 until allMembers.size).forEach { j ->
                val room = ChatRoom(
                    name = "TestRoom-${i + 1}-$j",
                    repositoryUrl = "https://github.com/test${i + 1}/repo$j",
                    inviteCode = UUID.randomUUID().toString()
                )

                repeat(min(3, allMembers.size)) { k ->
                    room.participants.add(
                        ChatParticipant(
                            participant = allMembers[k],
                            chatRoom = room
                        )
                    )
                }

                chatRoomRepository.save(room)
            }
        }
        chatRoomRepository.flush()
    }
}