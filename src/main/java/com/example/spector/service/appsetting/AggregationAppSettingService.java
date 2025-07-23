package com.example.spector.service.appsetting;

import com.example.spector.domain.AppSetting;
import com.example.spector.domain.dto.appsetting.AppSettingDTO;
import com.example.spector.mapper.BaseDTOConverter;
import com.example.spector.repositories.AppSettingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AggregationAppSettingService {
    private final AppSettingRepository appSettingRepository;
    private final BaseDTOConverter baseDTOConverter;

    public AppSettingDTO getSettings() {
        // Получаем первую запись или создаем новую с дефолтными значениями
        AppSetting settings = appSettingRepository.findFirstBy()
                .orElseGet(() -> {
                    AppSetting defaultSettings = new AppSetting();
                    defaultSettings.setPollActive(true);
                    defaultSettings.setAlarmActive(true);
                    defaultSettings.setPollPeriod(60);
                    return appSettingRepository.save(defaultSettings);
                });

        return baseDTOConverter.toDTO(settings, AppSettingDTO.class);
    }

    @Transactional
    public AppSettingDTO updateSettings(AppSettingDTO updateDTO) {
        // Получаем существующие настройки или создаем новые
        AppSetting settings = appSettingRepository.findFirstBy()
                .orElse(new AppSetting());

        // Обновляем только те поля, которые пришли в DTO
        if (updateDTO.getPollActive() != null) {
            settings.setPollActive(updateDTO.getPollActive());
        }
        if (updateDTO.getAlarmActive() != null) {
            settings.setAlarmActive(updateDTO.getAlarmActive());
        }
        if (updateDTO.getPollPeriod() != null) {
            settings.setPollPeriod(updateDTO.getPollPeriod());
        }

        AppSetting savedSettings = appSettingRepository.save(settings);
        return baseDTOConverter.toDTO(savedSettings, AppSettingDTO.class);
    }

    @Transactional
    public void resetToDefaults() {
        AppSetting settings = appSettingRepository.findFirstBy()
                .orElse(new AppSetting());

        settings.setPollActive(true);
        settings.setAlarmActive(true);
        settings.setPollPeriod(60);

        appSettingRepository.save(settings);
    }
}
