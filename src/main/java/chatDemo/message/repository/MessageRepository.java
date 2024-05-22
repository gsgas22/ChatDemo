package chatDemo.message.repository;

import chatDemo.message.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID>{
    List<Message> findByThreadId(String threadId);
    List<Message> findByThreadIdOrderByTimestampAsc(String threadId);
}
