package project.backend.auth.app

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import project.backend.auth.dto.CustomOAuth2User
import project.backend.auth.dto.OAuth2Attribute

@Service
@Transactional
class CustomOAuth2UserService(
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private val log = KotlinLogging.logger {}

    @Throws(OAuth2AuthenticationException::class)
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val delegate = DefaultOAuth2UserService()
        val oAuth2User = delegate.loadUser(userRequest)
        val accessToken = userRequest.accessToken.tokenValue
        val registrationId = userRequest.clientRegistration.registrationId

        val userNameAttributeName =
            userRequest.clientRegistration.providerDetails.userInfoEndpoint.userNameAttributeName

        log.info { "userNameAttributeName = $userNameAttributeName" }

        val customOAuth2User = CustomOAuth2User(oAuth2User, accessToken)

        val oAuth2Attribute =
            OAuth2Attribute.of(registrationId, userNameAttributeName, customOAuth2User.attributes)
        val memberAttribute = oAuth2Attribute.convertToMap()

        return DefaultOAuth2User(
            setOf(SimpleGrantedAuthority("ROLE_USER")),
            memberAttribute,
            userNameAttributeName
        )
    }
}