package com.example.spector.service;

import com.example.spector.domain.setting.AppSetting;
import com.example.spector.domain.setting.dto.AppSettingDto;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.mapper.AppSettingMapper;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.modules.event.EventMessage;
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
//    private final BaseDTOConverter baseDTOConverter;
    private final AppSettingMapper appSettingMapper;
    private final PollingManager pollingManager;

    public AppSettingDto getSettings() {
        // Получаем первую запись с дефолтными значениями
        AppSetting settings = appSettingRepository.findFirstBy()
                .orElseGet(() -> {
                    AppSetting defaultSettings = new AppSetting();
                    defaultSettings.setPollActive(true);
                    defaultSettings.setAlarmActive(true);
                    return appSettingRepository.save(defaultSettings);
                });

        return appSettingMapper.toAppSettingDto(settings);
    }

    @Transactional
    public AppSettingDto updateSettings(AppSettingDto updateDTO, String clientIp, EventDispatcher eventDispatcher) {
        // Получаем существующие настройки
        AppSetting settings = appSettingRepository.findFirstBy()
                .orElse(new AppSetting());

        // Сохраняем старое значение pollActive и AlarmActive для сравнения
        Boolean oldPollActive = settings.getPollActive();
        Boolean oldAlarmActive = settings.getAlarmActive();

        // Обновляем только те поля, которые пришли в DTO
        if (updateDTO.getPollActive() != null) {
            settings.setPollActive(updateDTO.getPollActive());
        }
        if (updateDTO.getAlarmActive() != null) {
            settings.setAlarmActive(updateDTO.getAlarmActive());
        }

        AppSetting savedSettings = appSettingRepository.save(settings);
        AppSettingDto responseDTO = appSettingMapper.toAppSettingDto(savedSettings);

        // Проверяем, изменилось ли состояние pollActive
        Boolean newPollActive = responseDTO.getPollActive();
        if (!Objects.equals(oldPollActive, newPollActive)) {
            if (Boolean.TRUE.equals(newPollActive)) {
                pollingManager.startAllPolling(); // Запускаем все задачи
                String message = String.format("IP %s: User enabled polling for all devices", clientIp);
                EventMessage event = EventMessage.log(EventType.REQUEST, MessageType.INFO, message);
                eventDispatcher.dispatch(event);
            } else {
                pollingManager.stopAllPolling(); // Останавливаем все задачи
                String message = String.format("IP %s: User disabled polling for all devices", clientIp);
                EventMessage event = EventMessage.log(EventType.REQUEST, MessageType.INFO, message);
                eventDispatcher.dispatch(event);
            }
        }

        // Проверяем, изменилось ли состояние alarmActive
        Boolean newAlarmActive = responseDTO.getAlarmActive();
        if (!Objects.equals(oldAlarmActive, newAlarmActive)) {
            if (Boolean.TRUE.equals(newAlarmActive)) {
                String message = String.format("IP %s: User enabled alarms", clientIp);
                EventMessage event = EventMessage.log(EventType.REQUEST, MessageType.INFO, message);
                eventDispatcher.dispatch(event);
            } else {
                String message = String.format("IP %s: User disabled alarms", clientIp);
                EventMessage event = EventMessage.log(EventType.REQUEST, MessageType.INFO, message);
                eventDispatcher.dispatch(event);
            }
        }

        return responseDTO;
    }

    @Transactional
    public void resetToDefaults(String clientIp, EventDispatcher eventDispatcher) {
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
                String message = String.format("IP %s: User reset settings: polling was re-enabled", clientIp);
                EventMessage event = EventMessage.log(EventType.REQUEST, MessageType.INFO, message);
                eventDispatcher.dispatch(event);
            } else {
                pollingManager.stopAllPolling();
                String message = String.format("IP %s: User reset settings: polling was disabled", clientIp);
                EventMessage event = EventMessage.log(EventType.REQUEST, MessageType.INFO, message);
                eventDispatcher.dispatch(event);
            }
        }
    }
}
