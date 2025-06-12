package project.backend.domain.member.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class PasswordChangeRequest(

    @field:NotBlank(message = "현재 비밀번호를 입력해주세요.")
    val currentPassword: String,

    @field:NotBlank(message = "새로운 비밀번호를 입력해주세요.")
    @field:Size(min = 4, message = "비밀번호는 최소 4자 이상이여야 합니다.")
    val newPassword: String,

    @field:NotBlank(message = "새로운 비밀번호 확인을 입력해주세요.")
    @field:Size(min = 4, message = "비밀번호는 최소 4자 이상이여야 합니다.")
    val confirmPassword: String

) {
    @get:AssertTrue(message = "비밀번호와 확인값이 일치하지 않습니다.")
    val isPasswordMatch: Boolean
        get() = newPassword == confirmPassword
}