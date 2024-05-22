package chatDemo.Authentication.service;

import chatDemo.Authentication.entity.OlxUser;
import chatDemo.Authentication.repository.OlxUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OlxUserService{
    @Autowired
    private OlxUserRepository olxUserRepository;

    public Optional<OlxUser> findUserById(Long id){
        return olxUserRepository.findById(id);
    }
    public OlxUser saveUser(OlxUser user){
        return olxUserRepository.save(user);
    }

    public void  deleteUser(Long id){
        olxUserRepository.deleteById(id);
    }
}
