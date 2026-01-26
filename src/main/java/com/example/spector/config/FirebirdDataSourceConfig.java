package com.example.spector.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FirebirdDataSourceConfig {
    // Используем @Value для получения значений из application.properties / application-{profile}.properties
    @Value("${firebird.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${firebird.datasource.jdbc-url}")
    private String url;

    @Value("${firebird.datasource.username}")
    private String username;

    @Value("${firebird.datasource.password}")
    private String password;

    @Bean(name = "firebirdDataSource")
    public DataSource firebirdDataSource() {
        return DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(url)
                .username(username)
                .password(password)
                .build();
    }
}

