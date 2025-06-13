package project.backend.global.security.handler.form

//import project.backend.global.exception.ex.BaseException.errorCode
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component
import project.backend.global.exception.ErrorResponse.Companion.toResponse
import project.backend.global.exception.errorcode.ErrorCode
import project.backend.global.exception.errorcode.LoginErrorCode
import project.backend.global.exception.ex.AuthException

@Component
class FormFailureHandler : AuthenticationFailureHandler {

    private val log = KotlinLogging.logger {}

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        val loginErrorCode = getLoginErrorCode(exception)
        log.info { "${loginErrorCode.code}, ${loginErrorCode.message}" }
        val errorResponse = toResponse(loginErrorCode)

        val json = ObjectMapper().writeValueAsString(errorResponse)
//
//        response.apply {
//            status = loginErrorCode.status.value()
//            contentType = "application/json"
//            characterEncoding = "UTF-8"
//            writer.write(json)
//        }

        response.status = loginErrorCode.status.value()
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"

        response.writer.write(json)
        response.writer.flush() //이거 왜있음?
    }

    private fun getLoginErrorCode(exception: AuthenticationException): ErrorCode =
        when {
            exception is InternalAuthenticationServiceException &&
                    exception.cause is AuthException -> {
                (exception.cause as AuthException).errorCode
            }

            exception is BadCredentialsException
                    || exception is InternalAuthenticationServiceException -> {
                LoginErrorCode.BAD_CREDENTIALS
            }

            exception is DisabledException -> LoginErrorCode.DISABLED
            exception is CredentialsExpiredException -> LoginErrorCode.CREDENTIALS_EXPIRED
            else -> LoginErrorCode.UNKNOWN
        }
}
