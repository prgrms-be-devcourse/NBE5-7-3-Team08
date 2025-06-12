package project.backend.global.webconfig

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**") // 모든 경로 허용
            .allowedOrigins("http://localhost:3000") // React dev server
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true)
    }
}

