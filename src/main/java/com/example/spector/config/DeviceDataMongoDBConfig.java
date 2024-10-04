package com.example.spector.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.example.spector.repositories.data", mongoTemplateRef = "deviceDataMongoTemplate")
public class DeviceDataMongoDBConfig {

    @Bean(name = "deviceDataMongoTemplate")
    @Primary
    public MongoTemplate deviceDataMongoTemplate(@Qualifier("deviceDataMongoDatabaseFactory") MongoDatabaseFactory mongoDbFactory) {
        return new MongoTemplate(mongoDbFactory);
    }

    @Bean(name = "deviceDataMongoDatabaseFactory")
    @Primary
    public MongoDatabaseFactory deviceDataMongoDatabaseFactory(@Qualifier("deviceDataMongoClient") MongoClient mongoClient) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, "device_data");
    }

    @Bean(name = "deviceDataMongoClient")
    public MongoClient deviceDataMongoClient() {
        return MongoClients.create("mongodb://localhost:27017");
    }
}

