package com.example.spector.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import static com.example.spector.config.BaseMongoConfig.DATABASE_DEVICE_DATA_MONGO_TEMPLATE;

@Configuration
@EnableMongoRepositories(
        basePackages = {"com.example.spector.repositories.data"},
        mongoTemplateRef = DATABASE_DEVICE_DATA_MONGO_TEMPLATE
)
public class DeviceDataMongoConfig {
}
