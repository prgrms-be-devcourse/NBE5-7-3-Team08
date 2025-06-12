package project.backend.domain.chat.chatmessage.dto

class ScrollPaginationCollection<T> private constructor(
    private val itemsWithNextCursor: List<T>,
    private val size: Int
) {
    companion object {
        fun <T> of(itemsWithNextCursor: List<T>, size: Int): ScrollPaginationCollection<T> {
            return ScrollPaginationCollection(itemsWithNextCursor, size)
        }
    }

    fun getCurrentScrollItems(): List<T> {
        return if (isLastScroll()) {
            itemsWithNextCursor
        } else {
            itemsWithNextCursor.subList(0, size)
        }
    }

    fun getNextCursor(): T? {
        return if (isLastScroll()) {
            null
        } else {
            itemsWithNextCursor[size - 1]
        }
    }

    fun isLastScroll(): Boolean {
        return itemsWithNextCursor.size <= size
    }
}