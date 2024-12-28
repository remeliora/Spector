package com.example.spector.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
@EnableConfigurationProperties(BaseMongoProperties.class)
public class BaseMongoConfig {
    protected static final String DATABASE_DEVICE_DATA_MONGO_TEMPLATE = "databaseDeviceDataMongoTemplate";
    protected static final String DATABASE_ENUMERATED_STATUS_MONGO_TEMPLATE = "databaseEnumeratedStatusMongoTemplate";

    @Bean(name = {DATABASE_DEVICE_DATA_MONGO_TEMPLATE})
    public MongoTemplate databaseDeviceDataMongoTemplate(MongoClient mongoClient, BaseMongoProperties mongoProperties) {
        return new MongoTemplate(mongoClient, mongoProperties.getDeviceData().getDatabase());
    }

    @Bean(name = {DATABASE_ENUMERATED_STATUS_MONGO_TEMPLATE})
    public MongoTemplate databaseEnumeratedStatusMongoTemplate(MongoClient mongoClient, BaseMongoProperties mongoProperties) {
        return new MongoTemplate(mongoClient, mongoProperties.getEnumeratedStatus().getDatabase());
    }

    @Bean
    public MongoClient mongoClient(BaseMongoProperties mongoProperties) {
        return MongoClients.create(getMongoClientSettings(mongoProperties));
    }

    private MongoClientSettings getMongoClientSettings(BaseMongoProperties mongoProperties) {
        return MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoProperties.getUri()))
                .build();
    }
}
