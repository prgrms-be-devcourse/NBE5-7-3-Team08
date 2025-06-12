package project.backend.chatroom.dao

import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import project.backend.domain.chat.chatroom.dao.ChatRoomRepository


private val log = KotlinLogging.logger {}

@SpringBootTest
class ChatRoomRepositoryTests @Autowired constructor(
    val chatRoomRepository: ChatRoomRepository
) {

    @Test
    fun `repository 주입 테스트`() {
        log.info { chatRoomRepository }
        assertThat(chatRoomRepository).isNotNull()
    }

}