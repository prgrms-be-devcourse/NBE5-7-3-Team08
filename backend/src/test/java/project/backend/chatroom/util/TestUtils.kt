package project.backend.chatroom.util

import project.backend.domain.chat.chatroom.entity.ChatParticipant
import project.backend.domain.chat.chatroom.entity.ChatRoom
import project.backend.domain.member.entity.Member
import project.backend.domain.member.entity.ProviderType
import java.time.LocalDateTime

fun createMember(
    id: Long,
    username: String = "user_${System.currentTimeMillis()}",
    nickname: String = "nickname",
    email: String? = "$username@example.com",
    password: String = "encodedPw",
    provider: ProviderType = ProviderType.LOCAL,
    profileImage: String = "default.png",
    recentRoomId: Long? = null
): Member {
    return Member(
        id = id,
        username = username,
        nickname = nickname,
        email = email,
        password = password,
        provider = provider,
        profileImage = profileImage,
        recentRoomId = recentRoomId
    )
}

fun createChatRoom(
    id: Long,
    name: String = "Test Room",
    repositoryUrl: String = "https://github.com/example/repo",
    inviteCode: String = "INV-${System.currentTimeMillis()}",
    createdAt: LocalDateTime = LocalDateTime.now()
): ChatRoom {
    return ChatRoom(
        id = id,
        name = name,
        repositoryUrl = repositoryUrl,
        inviteCode = inviteCode,
        createdAt = createdAt
    )
}

fun createParticipant(
    member: Member,
    chatRoom: ChatRoom,
    owner: Boolean = false,
    active: Boolean = true,
    joinAt: LocalDateTime = LocalDateTime.now()
): ChatParticipant {
    val participant = ChatParticipant(
        participant = member,
        chatRoom = chatRoom,
        owner = owner,
        joinAt = joinAt
    )

    chatRoom.participants.add(participant)
    return participant

}