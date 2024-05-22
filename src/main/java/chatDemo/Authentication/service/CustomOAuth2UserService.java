package chatDemo.Authentication.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Map;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Validate userRequest
        if (userRequest == null || userRequest.getAccessToken() == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_request"), "Invalid OAuth2UserRequest: userRequest or accessToken is null");
        }

        String userInfoEndpointUri = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUri();

        if (userInfoEndpointUri == null || userInfoEndpointUri.isEmpty()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_request"), "User info endpoint URI is empty");
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + userRequest.getAccessToken().getTokenValue());
        headers.add("Version", "2.0"); // Set the required 'Version' header
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(userInfoEndpointUri, HttpMethod.GET, entity, String.class);
        } catch (HttpClientErrorException e) {
            OAuth2Error oauth2Error = new OAuth2Error("invalid_user_info_response", e.getMessage(), null);
            throw new OAuth2AuthenticationException(oauth2Error, e.getMessage(), e);
        }

        String responseBody = response.getBody();
        if (responseBody == null || responseBody.isEmpty()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_user_info_response"), "User info response is empty");
        }

        Map<String, Object> userAttributes;
        try {
            // Parse the response body into a Map
            userAttributes = objectMapper.readValue(responseBody, Map.class);
        } catch (IOException e) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_user_info_response"), "Error parsing user info response", e);
        }

        // Extract user data from the nested "data" key
        if (userAttributes.containsKey("data")) {
            userAttributes = (Map<String, Object>) userAttributes.get("data");
        } else {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_user_info_response"), "Response does not contain user data");
        }

        // Assuming 'id' is the unique identifier for the user. Adjust if necessary.
        return new DefaultOAuth2User(
                null,
                userAttributes,
                "id"
        );
    }
}
