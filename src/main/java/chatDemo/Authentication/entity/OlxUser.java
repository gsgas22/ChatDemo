package chatDemo.Authentication.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OlxUser{
    @Id
    private Long id;
    private String email;
    private String status;
    private String name;
    private String phone;
    private String phoneLogin;
    private Boolean isBusiness;
}
