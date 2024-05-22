package chatDemo.Authentication.repository;

import chatDemo.Authentication.entity.OAuth2Token;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuth2TokenRepository extends JpaRepository<OAuth2Token, String>{
}
