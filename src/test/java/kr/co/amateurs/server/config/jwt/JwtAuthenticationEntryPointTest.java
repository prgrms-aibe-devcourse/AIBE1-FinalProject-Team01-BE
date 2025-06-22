package kr.co.amateurs.server.config.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationEntryPointTest {
    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authException;

    @InjectMocks
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() {
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    @Test
    void 인증_실패시_401_응답을_반환한다() throws Exception {
        // given
        given(request.getRequestURI()).willReturn("/api/test");
        given(authException.getMessage()).willReturn("Authentication failed");
        given(response.getWriter()).willReturn(printWriter);

        // when
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // then
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void 응답이_JSON_형태로_반환된다() throws Exception {
        // given
        given(request.getRequestURI()).willReturn("/api/test");
        given(authException.getMessage()).willReturn("Authentication failed");
        given(response.getWriter()).willReturn(printWriter);

        // when
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // then
        String jsonResponse = stringWriter.toString();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, Map.class);

        assertThat(responseMap.get("error")).isEqualTo("Unauthorized");
        assertThat(responseMap.get("message")).isEqualTo("인증이 필요합니다 로그인을 해주세요");
        assertThat(responseMap.get("status")).isEqualTo(401);
        assertThat(responseMap.get("path")).isEqualTo("/api/test");
    }
}
