package kr.co.amateurs.server.config;


import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;
import org.springframework.web.client.RestTemplate;

@TestConfiguration
@EnableSpringDataWebSupport(
        pageSerializationMode = PageSerializationMode.VIA_DTO
)
public class TestConfig {

    @Bean
    @Primary
    public RestTemplate restTemplate() {
        return Mockito.mock(RestTemplate.class);
    }

}