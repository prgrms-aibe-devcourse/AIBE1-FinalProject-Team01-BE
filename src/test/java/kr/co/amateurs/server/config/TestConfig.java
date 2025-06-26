package kr.co.amateurs.server.config;


import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;

@TestConfiguration
@EnableSpringDataWebSupport(
        pageSerializationMode = PageSerializationMode.VIA_DTO
)
public class TestConfig {
}