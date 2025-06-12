package project.backend.auth.app

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import project.backend.domain.member.app.MemberService
import project.backend.domain.member.entity.ProviderType
import project.backend.auth.dto.MemberDetails
import project.backend.global.exception.errorcode.AuthErrorCode
import project.backend.global.exception.ex.AuthException

@Service
@Transactional
class LoginService(
    private val memberService: MemberService
) : UserDetailsService {

    private val log = KotlinLogging.logger {}

    override fun loadUserByUsername(username: String): UserDetails {
        val foundMember = memberService.getMemberForLogin(username)

        if (foundMember.provider == ProviderType.GITHUB) {
            throw AuthException(AuthErrorCode.WRONG_AUTH_TYPE_LOGIN)
        }

        log.info{"로그인 시도 = $foundMember"}
        return MemberDetails(foundMember)
    }
}