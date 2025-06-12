package project.backend.global.exception.ex

import project.backend.global.exception.errorcode.MemberErrorCode

class MemberException(errorCode: MemberErrorCode) : BaseException(errorCode)
