package kr.co.amateurs.server.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.amateurs.server.config.SecurityConfig;
import kr.co.amateurs.server.domain.common.ErrorCode;
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

    @Test
    void 존재하지_않는_사용자로_로그인_시_404_에러가_발생해야_한다() throws Exception {
        // given
        LoginRequestDto request = LoginRequestDto.builder()
                .email("notfound@test.com")
                .password("password123")
                .build();

        given(authService.login(any(LoginRequestDto.class)))
                .willThrow(ErrorCode.USER_NOT_FOUND.get());

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."))
                .andExpect(jsonPath("$.statusMessage").value("Not Found"))
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    void 잘못된_비밀번호로_로그인_시_400_에러가_발생해야_한다() throws Exception {
        // given
        LoginRequestDto request = LoginRequestDto.builder()
                .email("test@test.com")
                .password("wrongpassword")
                .build();

        given(authService.login(any(LoginRequestDto.class)))
                .willThrow(ErrorCode.INVALID_PASSWORD.get());

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."))
                .andExpect(jsonPath("$.statusMessage").value("Bad Request"))
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void 잘못된_이메일_형식으로_로그인_시_400_에러가_발생해야_한다() throws Exception {
        // given
        LoginRequestDto request = LoginRequestDto.builder()
                .email("invalid-email")
                .password("password123")
                .build();

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void 빈_이메일로_로그인_시_400_에러가_발생해야_한다() throws Exception {
        // given
        LoginRequestDto request = LoginRequestDto.builder()
                .email("")
                .password("password123")
                .build();

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }
}
