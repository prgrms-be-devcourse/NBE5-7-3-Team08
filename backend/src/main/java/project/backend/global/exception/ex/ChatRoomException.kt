package project.backend.global.exception.ex

import project.backend.global.exception.errorcode.ChatRoomErrorCode

class ChatRoomException(errorCode: ChatRoomErrorCode) : BaseException(errorCode)
