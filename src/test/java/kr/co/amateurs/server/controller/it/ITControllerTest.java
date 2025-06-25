package kr.co.amateurs.server.controller.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.amateurs.server.config.TestSecurityConfig;
import kr.co.amateurs.server.service.it.ITService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ITControllerTest.class)
@Import({TestSecurityConfig.class})
public class ITControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ITService itService;

    @Autowired
    private ObjectMapper objectMapper;
}
