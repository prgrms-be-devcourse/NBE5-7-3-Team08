package project.backend.global.security.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.ExceptionTranslationFilter
import project.backend.auth.app.CustomOAuth2UserService
import project.backend.global.security.entrypoint.RestAuthenticationEntryPoint
import project.backend.global.security.filter.JwtAuthenticationFilter
import project.backend.global.security.handler.form.FormFailureHandler
import project.backend.global.security.handler.form.FormSuccessHandler
import project.backend.global.security.handler.oauth.OAuth2FailureHandler
import project.backend.global.security.handler.oauth.OAuth2SuccessHandler

@Configuration
class SecurityConfig(
    private val formFailureHandler: FormFailureHandler,
    private val formSuccessHandler: FormSuccessHandler,

    private val oAuth2SuccessHandler: OAuth2SuccessHandler,
    private val oAuth2FailureHandler: OAuth2FailureHandler,
    private val oAuth2UserService: CustomOAuth2UserService,

    private val restAuthenticationEntryPoint: RestAuthenticationEntryPoint,

    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder =
        PasswordEncoderFactories.createDelegatingPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .httpBasic { it.disable() }
            .csrf { it.disable() }
            .cors(Customizer.withDefaults())
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .formLogin {
                it.loginPage("/login")
                    .usernameParameter("username")
                    .passwordParameter("password")
                    .failureHandler(formFailureHandler)
                    .successHandler(formSuccessHandler)
                    .permitAll()
            }
            .authorizeHttpRequests {
                it.requestMatchers("/signup", "/login", "/login/oauth2/**", "/error")
                    .anonymous()

                    .requestMatchers("/token/**", "/github/**", "/images/**")
                    .permitAll()

                    .anyRequest()
                    .authenticated()
            }
            .oauth2Login {
                it.successHandler(oAuth2SuccessHandler)
                    .failureHandler(oAuth2FailureHandler)
                    .userInfoEndpoint { userInfoEndpoint ->
                        userInfoEndpoint.userService(oAuth2UserService)
                    }
            }
            .logout { it.disable() }
            .exceptionHandling {
                it.authenticationEntryPoint(restAuthenticationEntryPoint)
            }
            .addFilterAfter(jwtAuthenticationFilter, ExceptionTranslationFilter::class.java) //fixme
            .build()
}
