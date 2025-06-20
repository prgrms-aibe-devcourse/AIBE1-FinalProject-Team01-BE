package kr.co.amateurs.server.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.amateurs.server.config.SecurityConfig;
import kr.co.amateurs.server.domain.dto.auth.LoginRequestDto;
import kr.co.amateurs.server.domain.dto.auth.LoginResponseDto;
import kr.co.amateurs.server.service.auth.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 정산적인_로그인_요청_시_JWT_토큰이_반환되어야_한다() throws Exception {
        //given
        LoginRequestDto request = LoginRequestDto.builder()
                .email("test@test.com")
                .password("password123")
                .build();

        LoginResponseDto response = LoginResponseDto.of("accessToken123", 3600000L);

        given(authService.login(any(LoginRequestDto.class)))
                .willReturn(response);

        // When & then
        mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken123"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600000L));
    }
}
