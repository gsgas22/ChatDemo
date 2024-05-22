package chatDemo.message.service;

import chatDemo.message.entity.Attachment;
import chatDemo.message.repository.AttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AttachmentService {

    @Autowired
    private AttachmentRepository attachmentRepository;

    public Attachment saveAttachment(Attachment attachment) {
        return attachmentRepository.save(attachment);
    }

    public List<Attachment> getAllAttachments() {
        return attachmentRepository.findAll();
    }

    public Attachment getAttachmentById(UUID id) {
        return attachmentRepository.findById(id).orElse(null);
    }

    public void deleteAttachment(UUID id) {
        attachmentRepository.deleteById(id);
    }

    public List<Attachment> getAttachmentsByMessageId(UUID messageId) {
        return attachmentRepository.findByMessageId(messageId);
    }
}
