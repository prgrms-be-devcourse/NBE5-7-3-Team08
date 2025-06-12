package project.backend.domain.member.dto.event

import project.backend.domain.member.entity.Member

data class ProfileUpdateEvent(
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String
) {
    companion object {
        fun of(member: Member): ProfileUpdateEvent {
            return ProfileUpdateEvent(
                member.id!!,
                member.nickname,
                member.profileImage
            )
        }
    }
}