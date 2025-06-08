package project.backend.domain.chat.chatmessage.dao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import project.backend.domain.chat.chatmessage.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	List<ChatMessage> findByChatRoom_IdOrderBySendAtAsc(Long roomId);

	List<ChatMessage> findByIdIn(List<Long> ids);

	@Modifying
	@Query("DELETE FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId")
	void deleteByChatRoomId(Long roomId);
}
