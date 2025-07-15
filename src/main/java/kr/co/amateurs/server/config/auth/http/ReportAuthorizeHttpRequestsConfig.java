package kr.co.amateurs.server.config.auth.http;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class ReportAuthorizeHttpRequestsConfig implements CustomAuthorizeHttpRequestsConfigurer {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
                .requestMatchers(HttpMethod.GET, "/api/v1/reports/**").hasAnyRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/reports/**").hasAnyRole("ADMIN", "STUDENT", "GUEST")
                .requestMatchers(HttpMethod.PUT, "/api/v1/reports/**").hasAnyRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/reports/**").hasAnyRole("ADMIN");
    }
}
