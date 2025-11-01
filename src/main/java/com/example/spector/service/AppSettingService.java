package com.example.spector.service;

import com.example.spector.domain.AppSetting;
import com.example.spector.domain.dto.appsetting.AppSettingDTO;
import com.example.spector.mapper.BaseDTOConverter;
import com.example.spector.modules.polling.PollingManager;
import com.example.spector.repositories.AppSettingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AppSettingService {
    private final AppSettingRepository appSettingRepository;
    private final BaseDTOConverter baseDTOConverter;
    private final PollingManager pollingManager;

    public AppSettingDTO getSettings() {
        // Получаем первую запись или создаем новую с дефолтными значениями
        AppSetting settings = appSettingRepository.findFirstBy()
                .orElseGet(() -> {
                    AppSetting defaultSettings = new AppSetting();
                    defaultSettings.setPollActive(true);
                    defaultSettings.setAlarmActive(true);
                    return appSettingRepository.save(defaultSettings);
                });

        return baseDTOConverter.toDTO(settings, AppSettingDTO.class);
    }

    @Transactional
    public AppSettingDTO updateSettings(AppSettingDTO updateDTO) {
        // Получаем существующие настройки или создаем новые
        AppSetting settings = appSettingRepository.findFirstBy()
                .orElse(new AppSetting());

        // Сохраняем старое значение pollActive для сравнения
        Boolean oldPollActive = settings.getPollActive();

        // Обновляем только те поля, которые пришли в DTO
        if (updateDTO.getPollActive() != null) {
            settings.setPollActive(updateDTO.getPollActive());
        }
        if (updateDTO.getAlarmActive() != null) {
            settings.setAlarmActive(updateDTO.getAlarmActive());
        }

        AppSetting savedSettings = appSettingRepository.save(settings);
        AppSettingDTO responseDTO = baseDTOConverter.toDTO(savedSettings, AppSettingDTO.class);

        // Проверяем, изменилось ли состояние pollActive
        Boolean newPollActive = responseDTO.getPollActive();
        if (!Objects.equals(oldPollActive, newPollActive)) {
            if (Boolean.TRUE.equals(newPollActive)) {
                pollingManager.startAllPolling(); // Запускаем все задачи
            } else {
                pollingManager.stopAllPolling(); // Останавливаем все задачи
            }
        }

        return responseDTO;
    }

    @Transactional
    public void resetToDefaults() {
        AppSetting settings = appSettingRepository.findFirstBy()
                .orElse(new AppSetting());

        Boolean oldPollActive = settings.getPollActive();

        settings.setPollActive(true);
        settings.setAlarmActive(true);

        AppSetting savedSettings = appSettingRepository.save(settings);

        // Проверяем, изменилось ли состояние pollActive после сброса
        if (!Objects.equals(oldPollActive, savedSettings.getPollActive())) {
            if (Boolean.TRUE.equals(savedSettings.getPollActive())) {
                pollingManager.startAllPolling();
            } else {
                pollingManager.stopAllPolling();
            }
        }
    }
}
