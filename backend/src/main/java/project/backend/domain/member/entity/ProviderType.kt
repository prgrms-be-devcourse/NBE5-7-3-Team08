package project.backend.domain.member.entity

import com.fasterxml.jackson.annotation.JsonFormat

@JsonFormat(shape = JsonFormat.Shape.STRING)
enum class ProviderType {
    LOCAL,
    GITHUB
}
