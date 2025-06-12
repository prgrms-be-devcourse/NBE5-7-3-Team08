package project.backend.global.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import project.backend.global.exception.ErrorResponse.Companion.toResponse
import project.backend.global.exception.ex.BaseException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = KotlinLogging.logger {}

    @ExceptionHandler(BaseException::class)
    fun handleBaseException(ex: BaseException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(ex.status)
            .body(toResponse(ex.errorCode))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        ex: MethodArgumentNotValidException
    ): ResponseEntity<ErrorResponse> {
        log.info { "ex.getMessage() = ${ex.message}" }

        val bindingResult = ex.bindingResult

        //필드 에러
        bindingResult.fieldErrors.firstOrNull()?.let { fieldError ->
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(toResponse(fieldError))
        }

        //객체 에러(@AssertTrue 같은거)
        bindingResult.globalErrors.firstOrNull()?.let { globalError ->
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse("VALIDATION_FAILED", globalError.defaultMessage ?: "검증 실패"))
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse("UNEXPECTED_VALIDATION_FAILED", ex.message ?: "검증 실패")
            )
    }
}
