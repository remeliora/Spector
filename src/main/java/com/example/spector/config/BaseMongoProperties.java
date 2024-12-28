package com.example.spector.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Primary
@ConfigurationProperties(prefix = "spring.data.mongodb", ignoreUnknownFields = false)
public class BaseMongoProperties {
    @NotNull
    private MongoProperties deviceData;

    @NotNull
    private MongoProperties enumeratedStatus;

    @NotNull
    @NotEmpty
    private String uri;
}
