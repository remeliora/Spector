package com.example.spector.modules.handler;

import com.example.spector.database.mongodb.EnumeratedStatusService;
import com.example.spector.domain.ResultValue;
import com.example.spector.domain.dto.AppSettingDTO;
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
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EnumeratedParameterHandler implements ParameterHandler {
    private final EventDispatcher eventDispatcher;
    private final EnumeratedStatusService enumeratedStatusService;

    @Override
    public ResultValue handleParameter(DeviceDTO deviceDTO, ParameterDTO parameterDTO, Object value,
                                       List<ThresholdDTO> thresholdDTOList, AppSettingDTO appSettingDTO) {
        Integer intValue = (Integer) value;
        Map<Integer, String> statusMap = enumeratedStatusService.getStatusName(parameterDTO.getName());
        // Преобразуем фактическое значение в статусную строку
        String actualStatus = statusMap.getOrDefault(intValue, "Неизвестный ключ");
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
                int matchExact = thresholdDTO.getMatchExact();
                // Преобразуем допустимое значение порога в статусную строку, если возможно
                String allowedStatus = statusMap.getOrDefault(matchExact, String.valueOf(matchExact));
                if (matchExact != intValue) {
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
