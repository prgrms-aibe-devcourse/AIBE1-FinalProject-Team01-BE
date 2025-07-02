package kr.co.amateurs.server.config.auth;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class UserAuthorizeHttpRequestsConfig implements CustomAuthorizeHttpRequestsConfigurer{
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
                .requestMatchers("/api/v1/users/{userId}/bookmarks/**").hasAnyRole("ADMIN", "STUDENT","GUEST");
    }
}
