package chatDemo.Authentication.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2Token{
    @Id
    private String userId;
    private String accessToken;
    private String refreshToken;
    private Instant expiryTime;
}
