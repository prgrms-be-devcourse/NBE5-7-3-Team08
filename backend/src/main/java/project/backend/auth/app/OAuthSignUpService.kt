package project.backend.auth.app

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import project.backend.auth.dto.OAuthMemberDto
import project.backend.domain.member.dao.MemberRepository
import project.backend.domain.member.entity.Member
import project.backend.domain.member.mapper.MemberMapper
import project.backend.domain.imagefile.ImageFileService

@Service
class OAuthSignUpService(
    private val memberRepository: MemberRepository,
    private val imageFileService: ImageFileService,

    @Value("\${file.images.profile.default}")
    private val defaultProfileImg: String
) {

    fun oAuthSignUp(request: OAuthMemberDto): Member {
        return memberRepository.findByUsername(request.login)
            ?: memberRepository.save(MemberMapper.toEntity(request, defaultProfileImg))
    }
}