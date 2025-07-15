package kr.co.amateurs.server.config;

import kr.co.amateurs.server.config.auth.http.CustomAuthorizeHttpRequestsConfigurer;
import kr.co.amateurs.server.config.jwt.JwtAccessDeniedHandler;
import kr.co.amateurs.server.config.jwt.JwtAuthenticationEntryPoint;
import kr.co.amateurs.server.config.jwt.JwtAuthenticationFilter;
import kr.co.amateurs.server.service.auth.CustomOAuth2UserService;
import kr.co.amateurs.server.service.auth.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final List<CustomAuthorizeHttpRequestsConfigurer> customAuthorizeHttpRequestsConfigurers;

    @Bean
    @Profile("test")
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                .authorizeHttpRequests(auth -> {
                    customAuthorizeHttpRequestsConfigurers.forEach(configurer -> configurer.configure(auth));
                    auth.requestMatchers("/**").permitAll();
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }


    @Bean
    @Profile("!test")
    public SecurityFilterChain prodFilterChain(HttpSecurity http,
                                               CustomOAuth2UserService customOAuth2UserService,
                                               OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )

                .authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers("/ws/**").permitAll()
                            .requestMatchers("/app/**").permitAll()
                            .requestMatchers("/topic/**").permitAll()
                            .requestMatchers("/favicon.ico", "/").denyAll()
                            .requestMatchers("/error").permitAll()
                            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                            .requestMatchers("/actuator/**").permitAll()
                            .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll();

                    customAuthorizeHttpRequestsConfigurers.forEach(configurer -> configurer.configure(auth));

                    auth.anyRequest().denyAll();
                })


                // TODO: 개발 완료 후 아래 설정으로 변경할 예정
//                .authorizeHttpRequests(auth -> auth
//                        // 인증 없이 접근 가능
//                        .requestMatchers(
//                                "/api/v1/auth/**",
//                                "/swagger-ui/**",
//                                "/v3/api-docs/**",
//                                "/swagger-ui.html",
//                                "/actuator/**",
//                                "/oauth2/**",
//                                "/login/oauth2/**"
//                        ).permitAll()
//
//                        // 비로그인 사용자
//                        .requestMatchers(HttpMethod.GET,
//                                "/api/v1/reviews/**",
//                                "/api/v1/news/**",
//                                "/api/v1/projects/**"
//                        ).permitAll()
//
//                        // GUEST 이상
//                        .requestMatchers(HttpMethod.GET,
//                                "/api/v1/community/**")
//                        .hasAnyRole("GUEST", "STUDENT", "ADMIN")
//                        .requestMatchers(HttpMethod.POST,
//                                "/api/v1/community/**/like",
//                                "/api/v1/community/**/bookmark"
//                        ).hasAnyRole("GUEST", "STUDENT", "ADMIN")
//
//                        // STUDENT 이상
//                        .requestMatchers(HttpMethod.POST,
//                                "/api/v1/community/**")
//                        .hasAnyRole("STUDENT", "ADMIN")
//                        .requestMatchers(HttpMethod.PUT,
//                                "/api/v1/community/**")
//                        .hasAnyRole("STUDENT", "ADMIN")
//                        .requestMatchers(HttpMethod.DELETE,
//                                "/api/v1/community/**")
//                        .hasAnyRole("STUDENT", "ADMIN")
//
//                        .requestMatchers("/api/v1/**")
//                        .hasAnyRole("STUDENT", "ADMIN")
//
//                        // ADMIN만
//                        .requestMatchers("/api/admin/**")
//                        .hasRole("ADMIN")
//
//                        .anyRequest().authenticated()
//                )

                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
