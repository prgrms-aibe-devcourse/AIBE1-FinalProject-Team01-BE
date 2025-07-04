package kr.co.amateurs.server.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Slf4j
public class SwaggerConfig {

    @Value("${swagger.server.url:https://api.amateurs.co.kr/}")
    private String url;

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("Amatuers API Documentation");

        String jwtSchemeName = "JWT";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        var local = new Server().url("http://localhost:8080");
        var server = new Server().url(url);

        return new OpenAPI()
                .info(info)
                .servers(List.of(local, server))
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}
