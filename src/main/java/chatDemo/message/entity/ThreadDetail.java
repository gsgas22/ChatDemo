package chatDemo.message.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;
import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThreadDetail {

    @Id
    private UUID id;
    private Long userId;
    private String threadId;
    private String clientFullName;
    private String advertLogoUrl;
    private String lastMessageContent;
    private String lastMessageTimestamp;
}
