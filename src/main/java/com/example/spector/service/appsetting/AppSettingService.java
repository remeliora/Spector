package com.example.spector.service.appsetting;

import com.example.spector.domain.AppSetting;
import com.example.spector.repositories.AppSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppSettingService {
    private final AppSettingRepository appSettingRepository;

    public AppSetting getAppSetting() {
        return appSettingRepository.findById(1L) // Предположим, что настройки всегда хранятся в записи с ID = 1
                .orElseThrow(() -> new IllegalStateException("Настройки приложения не найдены"));
    }

//    public void updatePollPeriod(Integer newPeriod) {
//        AppSetting setting = getAppSetting(); // Получаем единственную запись
//        setting.setPollPeriod(newPeriod);    // Обновляем значение
//        appSettingRepository.save(setting);  // Сохраняем в базу
//    }

    public boolean isPollingActive() {
        AppSetting setting = getAppSetting();
        return Boolean.TRUE.equals(setting.getPollActive());
    }

    public Integer getPollPeriod() {
        AppSetting setting = getAppSetting();
        return setting.getPollPeriod();
    }
}
