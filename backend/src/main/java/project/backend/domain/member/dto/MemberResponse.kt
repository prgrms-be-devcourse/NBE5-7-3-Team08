package project.backend.domain.member.dto

import project.backend.domain.member.entity.ProviderType

data class MemberResponse (
    val id: Long,
    val username: String,
    val email: String?,
    val nickname: String,
    val provider: ProviderType,
    val profileImg: String
)