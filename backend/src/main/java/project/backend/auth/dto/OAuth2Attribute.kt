package project.backend.auth.dto

import org.slf4j.LoggerFactory
import project.backend.global.exception.errorcode.AuthErrorCode
import project.backend.global.exception.ex.AuthException

data class OAuth2Attribute(
    val attributes: Map<String, Any>,
    val attributeKey: String,  // login
    val name: String?,
    val email: String?,
    val githubAccess: String?
) {

    companion object {
        private val log = LoggerFactory.getLogger(OAuth2Attribute::class.java)

        fun of(provider: String, attributeKey: String, attributes: Map<String, Any>): OAuth2Attribute {
            return when (provider) {
                "github" -> ofGithub(attributeKey, attributes)
                else -> {
                    log.error("provider = {}", provider)
                    throw AuthException(AuthErrorCode.UNSUPPORTED_PROVIDER)
                }
            }
        }

        private fun ofGithub(attributeKey: String, attributes: Map<String, Any>): OAuth2Attribute {
            return OAuth2Attribute(
                attributes,
                attributes["login"] as? String ?: attributeKey,
                attributes["name"] as? String,
                attributes["email"] as? String,
                attributes["githubAccess"] as? String
            )
        }
    }

    fun convertToMap(): Map<String, Any?> {
        return mapOf(
            "login" to attributeKey,
            "name" to name,
            "email" to email,
            "githubAccess" to githubAccess
        )
    }
}