package project.backend.domain.chat.github

import project.backend.domain.chat.github.dto.GitRepoDto
import project.backend.global.exception.errorcode.GitHubErrorCode
import project.backend.global.exception.ex.GitHubException
import java.net.URI

object GitRepoUrlUtils {

    fun validateAndParseUrl(url: String): GitRepoDto {
        val uri = try {
            URI(url)
        } catch (e: Exception) {
            throw GitHubException(GitHubErrorCode.INVALID_REPO_RUL)
        }

        // 1. 도메인 체크
        if (uri.host != "github.com") {
            throw GitHubException(GitHubErrorCode.INVALID_REPO_RUL)
        }

        // 2. 경로 세그먼트 체크
        val segments = uri.path.split("/").filter { it.isNotBlank() }
        if (segments.size != 2) {
            throw GitHubException(GitHubErrorCode.INVALID_REPO_RUL)
        }

        return GitRepoDto(segments[0], segments[1])
    }
}
