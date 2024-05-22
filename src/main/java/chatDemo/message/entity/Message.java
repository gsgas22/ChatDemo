package chatDemo.message.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    private UUID id;
    private String threadId;
    private Long userId;
    private String marketplace;
    private String type;
    private String content;
    private String timestamp;

    @OneToMany(mappedBy = "messageId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments;
}
