package com.example.spector.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FirebirdDataSourceConfig {
    @Bean(name = "firebirdDataSource")
    public DataSource firebirdDataSource() {
        return DataSourceBuilder.create()
                .driverClassName(System.getProperty("FB_DRIVER"))
                .url(System.getProperty("FB_URL"))
                .username(System.getProperty("FB_USERNAME"))
                .password(System.getProperty("FB_PASSWORD"))
                .build();
    }
}

