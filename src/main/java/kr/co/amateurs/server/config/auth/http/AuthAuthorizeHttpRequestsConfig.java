package kr.co.amateurs.server.config.auth.http;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class AuthAuthorizeHttpRequestsConfig implements CustomAuthorizeHttpRequestsConfigurer {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
                .requestMatchers("/api/v1/auth/password/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/auth/check/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/signup").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").hasAnyRole("ADMIN", "STUDENT", "GUEST")
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/reissue").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/complete-profile").permitAll();
    }
}
