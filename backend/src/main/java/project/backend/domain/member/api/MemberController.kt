package project.backend.domain.member.api

import jakarta.validation.Valid
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import project.backend.domain.member.app.MemberService
import project.backend.domain.member.dto.MemberInfoUpdateRequest
import project.backend.domain.member.dto.MemberResponse
import project.backend.domain.member.dto.PasswordChangeRequest

@RestController
@RequestMapping("/user")
class MemberController(
    private val memberService: MemberService
) {

    @GetMapping("/details")
    fun getMemberDetails(authentication: Authentication): MemberResponse {
        return memberService.getMemberDetails(authentication)
    }

    @PutMapping("/info", consumes = ["multipart/form-data"])
    fun updateMemberInfo(
        authentication: Authentication,
        @RequestPart("request") @Valid request: MemberInfoUpdateRequest,
        @RequestPart("profileImg", required = false) profileImg: MultipartFile?
    ): MemberResponse {
        return memberService.updateMemberInfo(authentication, request, profileImg)
    }

    @PutMapping("/password")
    fun updatePassword(
        authentication: Authentication,
        @RequestBody @Valid request: PasswordChangeRequest
    ) {
        memberService.updatePassword(authentication, request)
    }
}