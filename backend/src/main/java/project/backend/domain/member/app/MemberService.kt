package project.backend.domain.member.app

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import project.backend.auth.dto.MemberDetails
import project.backend.domain.imagefile.ImageFileService
import project.backend.domain.member.dao.MemberRepository
import project.backend.domain.member.dto.MemberInfoUpdateRequest
import project.backend.domain.member.dto.MemberResponse
import project.backend.domain.member.dto.PasswordChangeRequest
import project.backend.domain.member.dto.SignUpRequest
import project.backend.domain.member.dto.event.ProfileUpdateEvent.Companion.of
import project.backend.domain.member.entity.Member
import project.backend.domain.member.entity.ProviderType
import project.backend.domain.member.mapper.MemberMapper.toEntity
import project.backend.domain.member.mapper.MemberMapper.toResponse
import project.backend.global.exception.errorcode.AuthErrorCode
import project.backend.global.exception.errorcode.MemberErrorCode
import project.backend.global.exception.ex.AuthException
import project.backend.global.exception.ex.MemberException

@Service
@Transactional
class MemberService (
    private val memberRepository: MemberRepository,
    private val imageFileService: ImageFileService,
    private val passwordEncoder: PasswordEncoder,
    private val eventPublisher: ApplicationEventPublisher,

){
    @Value("\${file.images.profile.default}")
    private lateinit var defaultProfileImg: String

    fun saveMember(request: SignUpRequest): MemberResponse {
        if (checkUsernameAlreadyExists(request.username)) {
            throw MemberException(MemberErrorCode.USERNAME_ALREADY_EXISTS)
        }

        if (request.email != null && checkEmailAlreadyExists(request.email)) {
            throw MemberException(MemberErrorCode.EMAIL_ALREADY_EXISTS)
        }

        val encryptedPassword = passwordEncoder.encode(request.password)

        val newMember = memberRepository.save(
            toEntity(request, encryptedPassword, defaultProfileImg)
        )

        return toResponse(newMember)
    }

    fun updateMemberInfo(
        auth: Authentication, request: MemberInfoUpdateRequest,
        file: MultipartFile?
    ): MemberResponse {
        val memberDetails = auth.principal as MemberDetails
        val targetMember = getMemberById(memberDetails.id)

        doUpdateMemberInfo(targetMember, request, file)

        eventPublisher.publishEvent(of(targetMember))

        return toResponse(targetMember)
    }

    private fun doUpdateMemberInfo(
        targetMember: Member, request: MemberInfoUpdateRequest,
        file: MultipartFile?
    ) {
        if (request.nickname != null) {
            targetMember.updateNickname(request.nickname)
        }

        if (request.email != null) {
            targetMember.updateEmail(request.email)
        }

        if (file != null) {
            val profileImage = imageFileService.saveProfileImage(file)
            targetMember.updateProfileImage(profileImage)
        }
    }

    fun updatePassword(auth: Authentication, request: PasswordChangeRequest) {
        val memberDetails = auth.principal as MemberDetails
        val targetMember = getMemberById(memberDetails.id)

        if (targetMember.provider != ProviderType.LOCAL) {
            throw AuthException(AuthErrorCode.WRONG_AUTH_TYPE_LOGIN)
        }

        val currentPassword = targetMember.password

        if (!passwordEncoder.matches(
                request.currentPassword,
                currentPassword
            )
        ) {
            throw MemberException(MemberErrorCode.WRONG_PASSWORD)
        }

        if (passwordEncoder.matches(request.newPassword, currentPassword)) {
            throw MemberException(MemberErrorCode.SAME_AS_OLD_PASSWORD)
        }

        targetMember.updatePassword(request.newPassword, passwordEncoder)
    }


    // Spring Security에서 UsernameNotFoundException을 처리하도록 유도하는 메서드
    fun getMemberForLogin(username: String): Member {
        try {
            return getMemberByUsername(username)
        } catch (e: MemberException) {
//            MemberService.log.info("존재하지 않는 username으로 로그인 시도: {}", username)
            throw UsernameNotFoundException("존재하지 않는 유저입니다: $username", e)
        }
    }

    fun getMemberByUsername(username: String): Member {
        return memberRepository.findByUsername(username)
            ?: throw MemberException(MemberErrorCode.MEMBER_NOT_FOUND)
    }

    fun getMemberById(id: Long): Member =
        memberRepository.findById(id)
            .orElseThrow { MemberException(MemberErrorCode.MEMBER_NOT_FOUND) }

    fun getMemberDetails(auth: Authentication): MemberResponse {
        val loginMember = auth.principal as MemberDetails
        val memberId = loginMember.id
//        MemberService.log.info("memberId = {}", memberId)
        val member = getMemberById(memberId)
        return toResponse(member)
    }

    private fun checkUsernameAlreadyExists(username: String): Boolean =
        memberRepository.findByUsername(username) != null

    private fun checkEmailAlreadyExists(email: String): Boolean =
        memberRepository.findByEmail(email) != null
}
