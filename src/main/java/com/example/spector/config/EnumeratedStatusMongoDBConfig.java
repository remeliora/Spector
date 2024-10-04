package com.example.spector.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.example.spector.repositories.status", mongoTemplateRef = "enumeratedStatusMongoTemplate")
public class EnumeratedStatusMongoDBConfig {

    @Bean(name = "enumeratedStatusMongoTemplate")
    public MongoTemplate enumeratedStatusMongoTemplate(@Qualifier("enumeratedStatusMongoDatabaseFactory") MongoDatabaseFactory mongoDbFactory) {
        return new MongoTemplate(mongoDbFactory);
    }

    @Bean(name = "enumeratedStatusMongoDatabaseFactory")
    public MongoDatabaseFactory enumeratedStatusMongoDatabaseFactory(@Qualifier("enumeratedStatusMongoClient") MongoClient mongoClient) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, "enumerated_status");
    }

    @Bean(name = "enumeratedStatusMongoClient")
    public MongoClient enumeratedStatusMongoClient() {
        return MongoClients.create("mongodb://localhost:27017");
    }
}

