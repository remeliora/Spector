package com.example.spector.config;

import com.example.spector.mdc.MDCTaskDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfigurer {
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(50);  // Основное количество потоков
        executor.setMaxPoolSize(100);   // Максимальное количество потоков
        executor.setQueueCapacity(1000); // Максимальная очередь задач
        executor.setThreadNamePrefix("AsyncExecutor-");
        // Устанавливаем декоратор для задач
        executor.setTaskDecorator(new MDCTaskDecorator());
        executor.initialize();
        return executor;
    }
}
