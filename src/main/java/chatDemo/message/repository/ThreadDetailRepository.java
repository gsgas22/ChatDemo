package chatDemo.message.repository;

import chatDemo.message.entity.ThreadDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ThreadDetailRepository extends JpaRepository<ThreadDetail, UUID> {
    List<ThreadDetail> findByUserId(Long userId);
}

