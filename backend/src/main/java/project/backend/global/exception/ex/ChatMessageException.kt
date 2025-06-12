package project.backend.global.exception.ex

import project.backend.global.exception.errorcode.ChatMessageErrorCode

class ChatMessageException(errorCode: ChatMessageErrorCode) : BaseException(errorCode)
