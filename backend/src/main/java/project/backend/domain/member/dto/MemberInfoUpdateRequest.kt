package project.backend.domain.member.dto

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import lombok.Getter
import project.backend.global.util.EmptyToNullDeserializer

@Getter
class MemberInfoUpdateRequest (
    @field: NotBlank(message = "닉네임을 공백으로 수정할 수 없습니다.")
    @field: Size(
        min = 3,
        message = "닉네임은 최소 3자 이상이여야 합니다."
    )
    val nickname: String,

    @field:Email(message = "올바른 이메일 형식이어야 합니다.")
    @JsonDeserialize(using = EmptyToNullDeserializer::class)
    val email: String?
)
