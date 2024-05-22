package chatDemo.message.service;

import chatDemo.message.entity.Message;
import chatDemo.message.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }

    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    public Message getMessageById(UUID id) {
        return messageRepository.findById(id).orElse(null);
    }

    public void deleteMessage(UUID id) {
        messageRepository.deleteById(id);
    }
    public List<Message> getMessagesByThreadId(String threadId) {
        return messageRepository.findByThreadId(threadId);
    }
}
