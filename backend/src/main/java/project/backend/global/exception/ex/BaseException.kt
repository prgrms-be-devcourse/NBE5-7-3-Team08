package project.backend.global.exception.ex

import org.springframework.http.HttpStatus
import project.backend.global.exception.errorcode.ErrorCode

abstract class BaseException(val errorCode: ErrorCode) : RuntimeException(
    errorCode.message
) {
    val status: HttpStatus //fixme getStatus() 고려 (추후 없애야하나?)
        get() = errorCode.status
}
