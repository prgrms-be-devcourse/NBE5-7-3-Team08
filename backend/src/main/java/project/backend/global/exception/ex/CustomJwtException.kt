package project.backend.global.exception.ex

import org.springframework.security.core.AuthenticationException
import project.backend.global.exception.errorcode.TokenErrorCode

class CustomJwtException(
    val errorCode: TokenErrorCode,
) : AuthenticationException(errorCode.message)
