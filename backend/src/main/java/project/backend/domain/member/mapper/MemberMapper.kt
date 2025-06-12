package project.backend.domain.member.mapper

import project.backend.auth.dto.OAuthMemberDto
import project.backend.domain.member.dto.MemberResponse
import project.backend.domain.member.dto.SignUpRequest
import project.backend.domain.member.entity.Member
import project.backend.domain.member.entity.ProviderType

object MemberMapper {

    fun toEntity(
        request: SignUpRequest, encryptedPassword: String, defaultProfileImg: String
    ): Member {
        return Member(
            username = request.username,
            email = request.email,
            password = encryptedPassword,
            nickname = request.nickname,
            provider = ProviderType.LOCAL,
            profileImage = defaultProfileImg
        )
    }

    fun toResponse(member: Member): MemberResponse {
        return MemberResponse(
            id = member.id!!,
            username = member.username,
            email = member.email,
            nickname = member.nickname,
            provider = member.provider,
            profileImg = member.profileImage
        )
    }

    fun toEntity(request: OAuthMemberDto, defaultProfileImg: String): Member {
        return Member(
            username = request.login,
            email = request.email,
            nickname = request.nickname,
            provider = ProviderType.GITHUB,
            profileImage = defaultProfileImg
        )
    }
}