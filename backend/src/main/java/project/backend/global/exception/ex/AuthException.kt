package project.backend.global.exception.ex

import project.backend.global.exception.errorcode.AuthErrorCode

class AuthException(errorCode: AuthErrorCode) : BaseException(errorCode)

