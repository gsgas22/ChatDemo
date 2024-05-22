package chatDemo.Authentication.service;

import chatDemo.Authentication.entity.OAuth2Token;
import chatDemo.Authentication.repository.OAuth2TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class OAuth2TokenService{

    @Autowired
    private OAuth2TokenRepository tokenRepository;

    public void saveToken(String userId, String accessToken, String refreshToken, Instant expiryTime){
        OAuth2Token token = new OAuth2Token(userId, accessToken, refreshToken,expiryTime);
        tokenRepository.save(token);
    }
    public Optional<OAuth2Token> getToken(String userId){
        return tokenRepository.findById(userId);
    }
}
