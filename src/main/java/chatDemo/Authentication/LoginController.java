package chatDemo.Authentication;

import chatDemo.Authentication.service.OAuth2AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController{

    @Autowired
    private OAuth2AuthenticationService authenticationService;

    @GetMapping("/loginSuccess")
    public String loginSuccess(OAuth2AuthenticationToken authentication){
        authenticationService.handleLoginSuccess(authentication);
        return "redirect:/";
    }

    @GetMapping("/loginFailure")
    public String loginFailure(){
        return "loginFailure";
    }
}
