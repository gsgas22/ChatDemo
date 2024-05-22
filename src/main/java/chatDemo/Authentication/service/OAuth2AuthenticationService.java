package chatDemo.Authentication.service;


import chatDemo.Authentication.entity.OAuth2Token;
import chatDemo.Authentication.entity.OlxUser;
import chatDemo.Authentication.repository.OlxUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class OAuth2AuthenticationService{
    @Autowired
    private OAuth2TokenService tokenService;
    @Autowired
    private OlxUserRepository olxUserRepository;
    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;
    public String getAccessTokenForService(Long userId, String service) {
        Optional<OAuth2Token> tokenOpt = tokenService.getToken(userId.toString());
        if (tokenOpt.isPresent()) {
            OAuth2Token token = tokenOpt.get();
            if (token.getExpiryTime().isAfter(Instant.now())) {
                return token.getAccessToken();
            } else {
                return refreshToken(userId, service, token.getRefreshToken());
            }
        }
        throw new RuntimeException("No valid access token found for user");
    }

    private String refreshToken(Long userId, String service, String refreshToken) {
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(service);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("refresh_token", refreshToken);
        params.add("client_id", clientRegistration.getClientId());
        params.add("client_secret", clientRegistration.getClientSecret());

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                clientRegistration.getProviderDetails().getTokenUri(),
                HttpMethod.POST,
                requestEntity,
                Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, Object> responseBody = response.getBody();
            String newAccessToken = (String) responseBody.get("access_token");
            if (newAccessToken != null) {
                Instant expiryTime = Instant.ofEpochSecond((Integer) responseBody.get("expires_in") + Instant.now().getEpochSecond());
                tokenService.saveToken(userId.toString(), newAccessToken, refreshToken, expiryTime);
                return newAccessToken;
            } else {
                System.err.println("Access token is null in the response");
                throw new RuntimeException("Access token is null in the response");
            }
        } else {
            System.err.println("Response is null or status code is not 2xx");
            throw new RuntimeException("Failed to refresh token");
        }
    }

    public void handleLoginSuccess(OAuth2AuthenticationToken authentication) {
        OAuth2User user =  authentication.getPrincipal();
        Long userId = Long.parseLong(user.getAttribute("id").toString());
        String email = user.getAttribute("email");
        String status = user.getAttribute("status");
        String name = user.getAttribute("name");
        String phone = user.getAttribute("phone").toString();
        String phoneLogin = user.getAttribute("phone_login").toString();
        Boolean isBusiness = Boolean.parseBoolean(user.getAttribute("is_business").toString());

        OlxUser olxUser = new OlxUser(userId, email, status, name, phone, phoneLogin, isBusiness);
        olxUserRepository.save(olxUser);

        // Extract and save OAuth2 token information
        String registrationId = authentication.getAuthorizedClientRegistrationId();
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(registrationId, authentication.getName());
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        String tokenValue = accessToken.getTokenValue();
        String refreshToken = authorizedClient.getRefreshToken().getTokenValue();
        Instant expiryTime = accessToken.getExpiresAt();

        // Save the token to the repository
        tokenService.saveToken(userId.toString(), tokenValue, refreshToken, expiryTime);
    }
}
