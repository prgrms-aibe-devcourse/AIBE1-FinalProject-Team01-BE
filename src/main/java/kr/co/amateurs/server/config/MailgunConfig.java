package kr.co.amateurs.server.config;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class MailgunConfig {

    @Value("${mailgun.api-key}")
    private String apiKey;

    @Value("${mailgun.domain}")
    private String domain;

    @Value("${mailgun.base-url}")
    private String baseUrl;

    @Bean
    public MailgunMessagesApi mailgunMessagesApi() {
        return MailgunClient.config(baseUrl, apiKey)
                .createApi(MailgunMessagesApi.class);
    }
}