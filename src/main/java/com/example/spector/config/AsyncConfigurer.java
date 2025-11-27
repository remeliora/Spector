package com.example.spector.config;

import com.example.spector.modules.mdc.MDCTaskDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfigurer {
    @Bean(name = "taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(50);  // Основное количество потоков
        executor.setMaxPoolSize(100);   // Максимальное количество потоков
        executor.setQueueCapacity(500); // Максимальная очередь задач
        executor.setThreadNamePrefix("AsyncExecutor-");
        // Устанавливаем декоратор для задач
        executor.setTaskDecorator(new MDCTaskDecorator());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(60); // Убивать idle потоки через 60 секунд
        executor.initialize();
        return executor;
    }
}
