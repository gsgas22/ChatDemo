package chatDemo.message;

import chatDemo.Authentication.service.OAuth2AuthenticationService;
import chatDemo.message.entity.ThreadDetail;
import chatDemo.message.service.ThreadDetailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ThreadDetailFetcher {

    @Autowired
    private OAuth2AuthenticationService authenticationService;

    @Autowired
    private ThreadDetailService threadDetailService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private final String OLX_API_BASE_URL = "https://www.olx.ua/api/partner";

    public void fetchAndSaveThreadDetails(Long userId, int count) {
        String accessToken = authenticationService.getAccessTokenForService(userId, "olx");
        List<Map<String, Object>> threadDetails = fetchThreadDetails(accessToken, count);

        if (threadDetails != null) {
            for (Map<String, Object> detail : threadDetails) {
                saveThreadDetail(userId, String.valueOf(detail.get("id")), accessToken);
            }
        } else {
            throw new RuntimeException("Failed to fetch thread details from OLX");
        }
    }

    private List<Map<String, Object>> fetchThreadDetails(String accessToken, int count) {
        String url = OLX_API_BASE_URL + "/threads?limit=" + count;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.add("Version", "2.0");

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                String responseBody = response.getBody();
                Map<String, Object> threadAttributes = objectMapper.readValue(responseBody, Map.class);

                if (threadAttributes.containsKey("data")) {
                    return (List<Map<String, Object>>) threadAttributes.get("data");
                } else {
                    throw new RuntimeException("No thread details found");
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse thread details response", e);
            }
        } else {
            throw new RuntimeException("Failed to fetch thread details from OLX");
        }
    }

    public void saveThreadDetail(Long userId, String threadId, String accessToken) {
        Map<String, Object> threadDetail = fetchThreadDetailById(threadId, accessToken);
        String interlocutorId = String.valueOf(threadDetail.get("interlocutor_id"));
        Map<String, Object> userDetail = fetchUserDetailById(interlocutorId, accessToken);
        String advertId = String.valueOf(threadDetail.get("advert_id"));
        Map<String, Object> advertDetail = fetchAdvertDetailById(advertId, accessToken);
        Map<String, Object> lastMessage = fetchLastMessageByThreadId(threadId, accessToken);

        // Extract the first image URL from the advertDetail
       // List<Map<String, Object>> images = (List<Map<String, Object>>) advertDetail.get("images");
        String advertLogoUrl = String.valueOf(advertDetail.get("title"));

        ThreadDetail threadDetailEntity = new ThreadDetail(
                UUID.randomUUID(),
                userId,
                threadId,
                (String) userDetail.get("name"),
                advertLogoUrl,
                (String) lastMessage.get("text"),
                (String) lastMessage.get("created_at")
        );

        threadDetailService.saveThreadDetail(threadDetailEntity);
    }

    private Map<String, Object> fetchThreadDetailById(String threadId, String accessToken) {
        String url = OLX_API_BASE_URL + "/threads/" + threadId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.add("Version", "2.0");

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                String responseBody = response.getBody();
                Map<String, Object> threadAttributes = objectMapper.readValue(responseBody, Map.class);

                if (threadAttributes.containsKey("data")) {
                    return (Map<String, Object>) threadAttributes.get("data");
                } else {
                    throw new RuntimeException("No thread detail found for thread ID: " + threadId);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse thread detail response", e);
            }
        } else {
            throw new RuntimeException("Failed to fetch thread detail for thread ID: " + threadId);
        }
    }

    private Map<String, Object> fetchUserDetailById(String userId, String accessToken) {
        String url = OLX_API_BASE_URL + "/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.add("Version", "2.0");

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                String responseBody = response.getBody();
                Map<String, Object> userAttributes = objectMapper.readValue(responseBody, Map.class);

                if (userAttributes.containsKey("data")) {
                    return (Map<String, Object>) userAttributes.get("data");
                } else {
                    throw new RuntimeException("No user detail found for user ID: " + userId);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse user detail response", e);
            }
        } else {
            throw new RuntimeException("Failed to fetch user detail for user ID: " + userId);
        }
    }

    private Map<String, Object> fetchAdvertDetailById(String advertId, String accessToken) {
        String url = OLX_API_BASE_URL + "/adverts/" + advertId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.add("Version", "2.0");

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                String responseBody = response.getBody();
                Map<String, Object> advertAttributes = objectMapper.readValue(responseBody, Map.class);

                if (advertAttributes.containsKey("data")) {
                    return (Map<String, Object>) advertAttributes.get("data");
                } else {
                    throw new RuntimeException("No advert detail found for advert ID: " + advertId);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse advert detail response", e);
            }
        } else {
            throw new RuntimeException("Failed to fetch advert detail for advert ID: " + advertId);
        }
    }

    private Map<String, Object> fetchLastMessageByThreadId(String threadId, String accessToken) {
        String url = OLX_API_BASE_URL + "/threads/" + threadId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.add("Version", "2.0");

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                String responseBody = response.getBody();
                Map<String, Object> messageAttributes = objectMapper.readValue(responseBody, Map.class);

                if (messageAttributes.containsKey("data")) {
                    List<Map<String, Object>> messages = (List<Map<String, Object>>) messageAttributes.get("data");
                    if (messages != null && !messages.isEmpty()) {
                        return messages.get(messages.size() - 1); // Останнє повідомлення
                    } else {
                        throw new RuntimeException("No messages found for thread ID: " + threadId);
                    }
                } else {
                    throw new RuntimeException("No messages found for thread ID: " + threadId);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse message detail response", e);
            }
        } else {
            throw new RuntimeException("Failed to fetch messages for thread ID: " + threadId);
        }
    }

}
