package chatDemo.message.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {

    @Id
    private UUID id;
    private UUID messageId;
    private String fileName;
    private String fileType;
    private byte[] data;
    private String timestamp;

    @ManyToOne
    @JoinColumn(name = "messageId", insertable = false, updatable = false)
    private Message message;
}
