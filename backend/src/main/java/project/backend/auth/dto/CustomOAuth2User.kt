package project.backend.auth.dto

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User

data class CustomOAuth2User(
    val oAuth2User: OAuth2User,
    val githubAccess: String
) : OAuth2User {

    override fun getAttributes(): Map<String, Any> =
        oAuth2User.attributes.toMutableMap().apply {
            put("githubAccess", githubAccess)
        }

    override fun getAuthorities(): Collection<GrantedAuthority> =
        oAuth2User.authorities

    override fun getName(): String = oAuth2User.name
}