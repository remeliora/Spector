package com.example.spector.modules.handler;

import com.example.spector.domain.ResultValue;
import com.example.spector.domain.dto.appsetting.AppSettingDTO;
import com.example.spector.domain.dto.device.DeviceDTO;
import com.example.spector.domain.dto.parameter.ParameterDTO;
import com.example.spector.domain.dto.threshold.ThresholdDTO;
import com.example.spector.domain.enums.AlarmType;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.modules.event.EventMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EnumeratedParameterHandler implements ParameterHandler {
    private final EventDispatcher eventDispatcher;

    @Override
    public ResultValue handleParameter(DeviceDTO deviceDTO, ParameterDTO parameterDTO, Object value,
                                       List<ThresholdDTO> thresholdDTOList, AppSettingDTO appSettingDTO) {
        String actualStatus = (String) value;
        String status = "OK";

        // Фильтруем пороги для текущего устройства
        List<ThresholdDTO> deviceThresholds = thresholdDTOList.stream()
                .filter(t -> t.getDevice().getId().equals(deviceDTO.getId()))
                .toList();

        // Если порогов нет для текущего устройства - сразу статус INACTIVE
        if (deviceThresholds.isEmpty()) {
            status = "INACTIVE";
        } else {
            for (ThresholdDTO thresholdDTO : deviceThresholds) {
                String allowedStatus = thresholdDTO.getMatchExact();
                if (!actualStatus.equals(allowedStatus)) {
                    status = "ERROR";
                    eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                            deviceDTO.getName() + ": " + parameterDTO.getDescription() + " = " + actualStatus
                            + ". Допустимое значение [" + allowedStatus + "]"));
                    eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                            thresholdDTO.getParameter().getName() + ": " +
                            " = " + actualStatus + ". Допустимое значение [" + allowedStatus + "]"));
                    eventDispatcher.dispatch(EventMessage.db(EventType.DB, MessageType.ERROR, AlarmType.EVERYWHERE,
                            appSettingDTO.getAlarmActive(), deviceDTO.getPeriod(),
                            deviceDTO.getName() + ": " + parameterDTO.getDescription() + " = " + actualStatus));
                }
            }
        }

        return new ResultValue(actualStatus, status);
    }
}
