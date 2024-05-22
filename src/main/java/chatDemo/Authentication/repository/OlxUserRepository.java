package chatDemo.Authentication.repository;

import chatDemo.Authentication.entity.OlxUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OlxUserRepository extends JpaRepository<OlxUser, Long>{
}
