package com.example.spector.service;

import com.example.spector.domain.setting.AppSetting;
import com.example.spector.domain.setting.dto.AppSettingDto;
import com.example.spector.mapper.AppSettingMapper;
import com.example.spector.modules.polling.PollingManager;
import com.example.spector.repositories.AppSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AppSettingServiceV1 {

    private final AppSettingMapper appSettingMapper;
    private final AppSettingRepository appSettingRepository;
    private final PollingManager pollingManager;

    @Transactional(readOnly = true)
    public AppSettingDto getAll() {
        Optional<AppSetting> appSettingOptional = appSettingRepository.findFirstBy();
        return appSettingMapper.toAppSettingDto(appSettingOptional.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found")));
    }

    @Transactional
    public AppSettingDto update(AppSettingDto dto) {
        AppSetting appSetting = appSettingRepository.findFirstBy().orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found"));

        Boolean oldPollActive = appSetting.getPollActive();
        Boolean oldAlarmActive = appSetting.getAlarmActive();

        appSettingMapper.updateWithNull(dto, appSetting);
        AppSetting resultAppSetting = appSettingRepository.save(appSetting);

        handlePollingChange(oldPollActive, resultAppSetting.getPollActive());
        handleAlarmsChange(oldAlarmActive, resultAppSetting.getAlarmActive());

        return appSettingMapper.toAppSettingDto(resultAppSetting);
    }

    private void handleAlarmsChange(Boolean oldAlarmActive, Boolean alarmActive) {
    }

    private void handlePollingChange(Boolean oldPollActive, Boolean pollActive) {
        if (!Objects.equals(oldPollActive, pollActive)) {
            if (Boolean.TRUE.equals(pollActive)) {
                pollingManager.startAllPolling();
            } else {
                pollingManager.stopAllPolling();
            }
        }
    }
}
