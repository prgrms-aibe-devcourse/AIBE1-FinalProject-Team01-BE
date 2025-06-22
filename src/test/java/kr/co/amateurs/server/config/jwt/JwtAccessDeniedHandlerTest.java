package kr.co.amateurs.server.config.jwt;

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
import org.springframework.security.access.AccessDeniedException;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class JwtAccessDeniedHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AccessDeniedException accessDeniedException;

    @InjectMocks
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() {
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    @Test
    void 접근_거부시_403_응답을_반환한다() throws Exception {
        // given
        given(request.getRequestURI()).willReturn("/api/admin");
        given(accessDeniedException.getMessage()).willReturn("Access denied");
        given(response.getWriter()).willReturn(printWriter);

        // when
        jwtAccessDeniedHandler.handle(request, response, accessDeniedException);

        // then
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
    }
}
