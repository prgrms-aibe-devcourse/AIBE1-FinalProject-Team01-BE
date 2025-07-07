package kr.co.amateurs.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tesseract")
@Data
public class TesseractConfig {
    
    private String dataPath = "classpath:testdata";
    private String language = "kor+eng";
    private String fallbackLanguage = "eng";
}
