package project.backend.global.exception.ex

import project.backend.global.exception.errorcode.ErrorCode

class GitHubException(errorCode: ErrorCode) : BaseException(errorCode)
