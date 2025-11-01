package com.example.spector.config;


import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                        .maximumSize(1000) // Максимальный размер кэша (настройте под ваше количество устройств)
                        .expireAfterWrite(5, TimeUnit.MINUTES) // Время жизни записи в кэше после записи
                        .recordStats() // Для мониторинга (опционально)
        );

        return cacheManager;
    }
}
