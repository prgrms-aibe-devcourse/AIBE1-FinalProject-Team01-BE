package kr.co.amateurs.server.config;

import kr.co.amateurs.server.config.jwt.CustomUserDetailsService;
import kr.co.amateurs.server.config.jwt.JwtProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static org.mockito.Mockito.mock;

@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
public class TestSecurityConfig {

    @Bean
    @Primary  // 테스트에서 우선적으로 사용
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    @Bean
    @Primary
    public JwtProvider mockJwtProvider() {
        return mock(JwtProvider.class);
    }

    @Bean
    @Primary
    public CustomUserDetailsService mockCustomUserDetailsService() {
        return mock(CustomUserDetailsService.class);
    }
}