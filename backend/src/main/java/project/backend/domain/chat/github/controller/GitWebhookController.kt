package project.backend.domain.chat.github.controller

import org.springframework.web.bind.annotation.*
import project.backend.domain.chat.github.app.GitMessageService

@RestController
@RequestMapping("/github")
class GitWebhookController(
    private val gitMessageService: GitMessageService
) {

    @PostMapping("/{roomId}")
    fun handleWebhook(
        @PathVariable roomId: Long,
        @RequestBody payload: Map<String, Any>,
        @RequestHeader("X-GitHub-Event") eventType: String
    ) {
        gitMessageService.handleEvent(roomId, eventType, payload)
    }
}
