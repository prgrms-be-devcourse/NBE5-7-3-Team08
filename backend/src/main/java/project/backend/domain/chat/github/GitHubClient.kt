package project.backend.domain.chat.github

import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import project.backend.global.exception.errorcode.GitHubErrorCode
import project.backend.global.exception.ex.GitHubException
import reactor.core.publisher.Mono

@Component
class GitHubClient(
    private val webClientBuilder: WebClient.Builder
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    fun validateAdminPermission(accessToken: String, owner: String, repo: String): Boolean {
        val url = "https://api.github.com/repos/$owner/$repo"

        val response: Map<String, Any>? = webClientBuilder.build()
            .get()
            .uri(url)
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError) { error ->
                error.bodyToMono(String::class.java).flatMap { errorBody ->
                    when (error.statusCode()) {
                        HttpStatus.UNAUTHORIZED -> {
                            log.error(errorBody)
                            Mono.error(GitHubException(GitHubErrorCode.INVALID_TOKEN))
                        }
                        HttpStatus.NOT_FOUND -> {
                            log.error(errorBody)
                            Mono.error(GitHubException(GitHubErrorCode.REPO_NOT_FOUND))
                        }
                        else -> {
                            log.error("GitHubErrorCode.CLIENT_ERROR: {}", errorBody)
                            Mono.error(GitHubException(GitHubErrorCode.CLIENT_ERROR))
                        }
                    }
                }
            }
            .onStatus(HttpStatusCode::is5xxServerError) { error ->
                error.bodyToMono(String::class.java).flatMap { errorBody ->
                    log.error("GitHubErrorCode.SERVER_ERROR: {}", errorBody)
                    Mono.error(GitHubException(GitHubErrorCode.SERVER_ERROR))
                }
            }
            .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
            .block()

        val permissions = response?.get("permissions") as? Map<*, *> ?: throw GitHubException(GitHubErrorCode.UNEXPECTED_RESPONSE)
        val isAdmin = permissions["admin"] as? Boolean ?: false

        if (!isAdmin) {
            throw GitHubException(GitHubErrorCode.UNAUTHORIZED_REPO)
        }

        return true
    }

    fun registerWebhook(accessToken: String, owner: String, repo: String, webhookUrl: String) {
        val apiUrl = "https://api.github.com/repos/$owner/$repo/hooks"

        val requestBody = mapOf(
            "name" to "web",
            "active" to true,
            "events" to listOf("issues", "pull_request", "pull_request_review"),
            "config" to mapOf(
                "url" to webhookUrl,
                "content_type" to "json",
                "insecure_ssl" to "0"
            )
        )

        try {
            webClientBuilder.build()
                .post()
                .uri(apiUrl)
                .header("Authorization", "Bearer $accessToken")
                .header("Accept", "application/vnd.github.v3+json")
                .bodyValue(requestBody)
                .retrieve()
                .toBodilessEntity()
                .block()
        } catch (e: Exception) {
            log.error("웹훅 등록 중 예외 발생", e)
            throw GitHubException(GitHubErrorCode.WEBHOOK_REGISTER_FAILED)
        }
    }

    private fun getWebClient(gitHubAccessToken: String): WebClient {
        return WebClient.builder()
            .baseUrl("https://api.github.com")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $gitHubAccessToken")
            .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
            .build()
    }

    fun getPrivateEmail(gitHubAccessToken: String): String {
        val emailList: List<Map<String, Any>> = getWebClient(gitHubAccessToken)
            .get()
            .uri("/user/emails")
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<List<Map<String, Any>>>() {})
            .block()
            ?: throw IllegalStateException("GitHub 이메일 정보를 불러올 수 없습니다.")

        val primaryEmail = emailList.firstOrNull { it["primary"] == true }?.get("email") as? String
        return primaryEmail ?: (emailList.first()["email"]?.toString()
            ?: throw IllegalStateException("이메일 데이터가 존재하지 않습니다."))
    }
}
