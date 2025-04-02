package com.example.spector.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class PostgresDataSourceConfig {
    @Primary
    @Bean(name = "postgresDataSource")
    public DataSource postgresDataSource() {
        return DataSourceBuilder.create()
                .driverClassName(System.getProperty("DB_POSTGRES_DRIVER"))
                .url(System.getProperty("DB_POSTGRES_URL"))
                .username(System.getProperty("DB_POSTGRES_USERNAME"))
                .password(System.getProperty("DB_POSTGRES_PASSWORD"))
                .build();
    }
}
