package com.example;

import com.example.dto.UserInfo;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.http.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.User;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@EnableOAuth2Sso
@RestController
public class FacebookController extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/**").authorizeRequests()
                .antMatchers("/", "/login**").permitAll()
                .anyRequest().authenticated()
                .and().logout().logoutSuccessUrl("/").permitAll();
    }

    @RequestMapping("/")
    public Map<String, String > index(Principal principal) {
        Map<String, String> map = new HashMap<>();
        if (principal == null) {
            map.put("Stan zalogowania", "Niezalogowany");
        }
        else {
            map.put("Stan zalogowania", "Zalogowany");
        }
        return map;
    }

    @RequestMapping("api")
    public Map<String, String> api(Principal principal) {
        Map<String, String> map = new HashMap<>();
        String token = getToken(principal);

        FacebookTemplate facebookTemplate = new FacebookTemplate(token);
        User user = facebookTemplate.userOperations().getUserProfile();
        map.put("id", user.getId());
        map.put("name", user.getName());
        map.put("email", user.getEmail());
        return map;
    }

    @RequestMapping("rest")
    public Map<String, String> rest(Principal principal) {
        Map<String, String> map = new HashMap<>();
        String token = getToken(principal);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String dataUrl = "https://graph.facebook.com/v2.6/me?fields=id,name,email";
        RestTemplate template = new RestTemplate();
        ResponseEntity<UserInfo> response = template.exchange(dataUrl, HttpMethod.GET, entity, UserInfo.class);
        map.put("name", response.getBody().getName());
        map.put("id", response.getBody().getId());
        map.put("email", response.getBody().getEmail());
        return map;
    }

    private String getToken(Principal principal) {
        OAuth2Authentication oAuth2Authentication = (OAuth2Authentication)principal;
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails)oAuth2Authentication.getDetails();
        return details.getTokenValue();
    }
}

