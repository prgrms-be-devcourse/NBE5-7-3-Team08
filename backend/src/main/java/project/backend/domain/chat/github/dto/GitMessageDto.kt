package project.backend.domain.chat.github.dto

import project.backend.domain.chat.chatroom.entity.ChatRoom

data class GitMessageDto(
    val type: GitEventType,
    val actor: String,
    val content: String,
    var room: ChatRoom? = null
) {
    companion object {
        fun fromIssue(payload: Map<String, Any>): GitMessageDto? {
            val action = payload["action"] as? String ?: return null
            if (action != "opened") return null

            val issue = payload["issue"] as? Map<*, *> ?: return null
            val sender = payload["sender"] as? Map<*, *> ?: return null

            val title = issue["title"] as? String ?: return null
            val url = issue["html_url"] as? String ?: return null
            val author = sender["login"] as? String ?: return null

            val content = "[ISSUE $action] $title by $author\n$url"
            return of(GitEventType.ISSUE_OPEN, author, content)
        }

        fun fromPullRequest(payload: Map<String, Any>): GitMessageDto? {
            val action = payload["action"] as? String ?: return null
            val pr = payload["pull_request"] as? Map<*, *> ?: return null
            val sender = payload["sender"] as? Map<*, *> ?: return null

            val title = pr["title"] as? String ?: return null
            val url = pr["html_url"] as? String ?: return null
            val author = sender["login"] as? String ?: return null

            return when {
                action == "opened" -> {
                    val content = "[PR opened] $title by $author\n$url"
                    of(GitEventType.PR_OPEN, author, content)
                }
                action == "closed" && pr["merged"] == true -> {
                    val fromBranch = (pr["head"] as? Map<*, *>)?.get("ref") as? String ?: return null
                    val toBranch = (pr["base"] as? Map<*, *>)?.get("ref") as? String ?: return null

                    val content = "[PR merged] $title by $author\n" +
                            "merged to $toBranch from $fromBranch\n$url"
                    of(GitEventType.PR_MERGED, author, content)
                }
                else -> null
            }
        }

        fun fromPullRequestReview(payload: Map<String, Any>): GitMessageDto? {
            val action = payload["action"] as? String ?: return null
            if (action != "submitted") return null

            val review = payload["review"] as? Map<*, *> ?: return null
            val pr = payload["pull_request"] as? Map<*, *> ?: return null

            val reviewer = (review["user"] as? Map<*, *>)?.get("login") as? String ?: return null
            val state = review["state"] as? String ?: return null
            val reviewUrl = review["html_url"] as? String ?: return null
            val prTitle = pr["title"] as? String ?: return null
            val body = review["body"] as? String?

            val content = "[PR review: $state] $prTitle review by $reviewer" +
                    (if (!body.isNullOrBlank()) "\n$body" else "") + "\n$reviewUrl"

            return of(GitEventType.PR_REVIEW, reviewer, content)
        }

        fun of(type: GitEventType, actor: String, content: String): GitMessageDto {
            return GitMessageDto(type, actor, content)
        }
    }

    fun attachRoom(room: ChatRoom): GitMessageDto {
        this.room = room
        return this
    }
}
