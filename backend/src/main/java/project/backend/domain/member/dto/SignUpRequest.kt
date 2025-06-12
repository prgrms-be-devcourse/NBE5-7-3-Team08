package project.backend.domain.member.dto

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import project.backend.global.util.EmptyToNullDeserializer

data class SignUpRequest (

    @field: NotBlank(message = "ID를 입력해주세요.")
    @field: Size(
        min = 5,
        max = 12,
        message = "ID는 최소 5자 이상, 12자 이하여야 합니다."
    )
    var username: String,

    @field: NotBlank(message = "닉네임을 입력해주세요.")
    @field: Size(
        min = 3,
        message = "닉네임은 최소 3자 이상이여야 합니다."
    )
    var nickname: String,

    @field: Email(message = "올바른 이메일 형식이어야 합니다.")
    @JsonDeserialize(using = EmptyToNullDeserializer::class)
    var email: String? = null,

    @field: NotBlank(message = "비밀번호를 입력해주세요.")
    @field: Size(
        min = 4,
        message = "비밀번호는 최소 4자 이상이여야 합니다."
    )
    var password: String
)
