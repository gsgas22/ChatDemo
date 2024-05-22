package chatDemo.message;

import chatDemo.Authentication.service.OAuth2AuthenticationService;
import chatDemo.message.entity.Attachment;
import chatDemo.message.entity.Message;
import chatDemo.message.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MessageManager {

    @Autowired
    private OAuth2AuthenticationService authenticationService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private final String OLX_API_BASE_URL = "https://www.olx.ua/api/partner";

    // Method to fetch all messages from a specific thread and save them in the database
    public void fetchAndSaveMessages(Long userId, String threadId) {
        String accessToken = authenticationService.getAccessTokenForService(userId, "olx");
        List<Map<String, Object>> messages = fetchMessages(threadId, accessToken);

        if (messages != null) {
            for (Map<String, Object> messageData : messages) {
                Message message = new Message(
                        UUID.randomUUID(),
                        threadId,
                        userId,
                        "olx",
                        (String) messageData.get("type"),
                        (String) messageData.get("text"),
                        (String) messageData.get("created_at"),
                        (List<Attachment>) messageData.get("attachments")
                );
                messageService.saveMessage(message);
            }
        } else {
            throw new RuntimeException("Failed to fetch messages from OLX");
        }
    }

    private List<Map<String, Object>> fetchMessages(String threadId, String accessToken) {
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
                    return (List<Map<String, Object>>) messageAttributes.get("data");
                } else {
                    throw new RuntimeException("No messages found for thread ID: " + threadId);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse message details response", e);
            }
        } else {
            throw new RuntimeException("Failed to fetch messages for thread ID: " + threadId);
        }
    }

    // Method to post a new message in a specific thread
    public void postMessage(Long userId, String threadId, String messageText) {
        String accessToken = authenticationService.getAccessTokenForService(userId, "olx");
        String url = OLX_API_BASE_URL + "/threads/" + threadId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/json");
        headers.add("Version", "2.0");

        Map<String, String> messageBody = Map.of("text", messageText);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(messageBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to post message to OLX");
        }
    }
}
