package com.example.spector.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import static com.example.spector.config.BaseMongoConfig.DATABASE_ENUMERATED_STATUS_MONGO_TEMPLATE;

@Configuration
@EnableMongoRepositories(
        basePackages = {"com.example.spector.repositories.status"},
        mongoTemplateRef = DATABASE_ENUMERATED_STATUS_MONGO_TEMPLATE
)
public class EnumeratedStatusMongoConfig {
}
