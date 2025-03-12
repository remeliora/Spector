package com.example.spector.modules.mdc;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

@Slf4j
public class MDCTaskDecorator implements TaskDecorator {
    @Override
    public @NonNull Runnable decorate(@NonNull Runnable runnable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap(); // Сохраняем контекст MDC
        return () -> {
            if (contextMap != null) {
                MDC.setContextMap(contextMap); // Восстанавливаем контекст
            }
            try {
                runnable.run();
            } finally {
                MDC.clear(); // Очищаем контекст
            }
        };
    }
}
