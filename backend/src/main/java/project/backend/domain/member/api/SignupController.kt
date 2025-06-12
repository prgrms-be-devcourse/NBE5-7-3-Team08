package project.backend.domain.member.api

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import project.backend.domain.member.app.MemberService
import project.backend.domain.member.dto.MemberResponse
import project.backend.domain.member.dto.SignUpRequest

@RestController
@RequestMapping("/signup")
class SignupController(
    private val memberService: MemberService
) {
    private val log = KotlinLogging.logger {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun signup(@RequestBody @Valid request: SignUpRequest): MemberResponse {
        log.info{"request = $request"}
        return memberService.saveMember(request)
    }
}