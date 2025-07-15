package kr.co.amateurs.server.config.auth.http;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class UserAuthorizeHttpRequestsConfig implements CustomAuthorizeHttpRequestsConfigurer {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
                .requestMatchers("/api/v1/users/me/**").hasAnyRole("ADMIN", "STUDENT", "GUEST")
                .requestMatchers("/api/v1/users/{userId}/bookmarks/**").hasAnyRole("ADMIN", "STUDENT","GUEST");
    }
}
