package chatDemo.message.service;

import chatDemo.message.entity.ThreadDetail;
import chatDemo.message.repository.ThreadDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ThreadDetailService {

    @Autowired
    private ThreadDetailRepository threadDetailRepository;

    public List<ThreadDetail> getThreadDetailsByUserId(Long userId) {
        return threadDetailRepository.findByUserId(userId);
    }

    public void saveThreadDetail(ThreadDetail threadDetail) {
        threadDetailRepository.save(threadDetail);
    }

    public ThreadDetail getThreadDetailById(UUID id) {
        return threadDetailRepository.findById(id).orElse(null);
    }

    public void deleteThreadDetail(UUID id) {
        threadDetailRepository.deleteById(id);
    }
}
