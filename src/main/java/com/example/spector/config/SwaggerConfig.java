package com.example.spector.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    @Value("${swagger.enabled:true}")
    private boolean swaggerEnabled;

    @Value("${swagger.server-url:http://localhost:8080}")
    private String serverUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        if (!swaggerEnabled) {
            return new OpenAPI().info(new Info().title("API Disabled").version("1.0.0"));
        }

        return new OpenAPI()
                // Серверы
                .servers(List.of(
                        new Server()
                                .url(serverUrl)
                                .description("Production Server"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")
                ))

                // Информация о API
                .info(new Info()
                        .title("SPECTOR API")
                        .version("1.0.0")
                        .description("Документация REST API для Spring Boot приложения Spector")
                        .contact(new Contact()
                                .name("Developer Email")
                                .email("remeliora2020@gmail.com")));
    }
}
