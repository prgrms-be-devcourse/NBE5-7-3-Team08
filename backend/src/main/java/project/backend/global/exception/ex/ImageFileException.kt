package project.backend.global.exception.ex

import project.backend.global.exception.errorcode.ImageFileErrorCode

class ImageFileException(errorCode: ImageFileErrorCode) : BaseException(errorCode)
