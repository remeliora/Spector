package com.example.spector.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

@Configuration
public class DotenvConfigurer implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            Dotenv dotenv = Dotenv.load();
            dotenv.entries().forEach(entry ->
                    System.setProperty(entry.getKey(), entry.getValue()));
        } catch (Exception e) {
            // Логируем, но не падаем
            System.out.println("Note: .env file not found, using environment variables only");
        }
    }
}
