package com.example.spector.modules.handler;

import com.example.spector.domain.device.Device;
import com.example.spector.domain.enums.AlarmType;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.domain.parameter.Parameter;
import com.example.spector.domain.setting.AppSetting;
import com.example.spector.domain.threshold.Threshold;
import com.example.spector.modules.datapattern.ResultValue;
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
    public ResultValue handleParameter(Device device, Parameter parameter, Object value,
                                       List<Threshold> thresholdList, AppSetting appSetting) {
        String actualStatus = (String) value;
        String status = "OK";

        // Фильтруем пороги для текущего устройства
        List<Threshold> deviceThresholds = thresholdList.stream()
                .filter(t -> t.getDevice().getId().equals(device.getId()))
                .toList();

        // Если порогов нет для текущего устройства - сразу статус INACTIVE
        if (deviceThresholds.isEmpty()) {
            status = "INACTIVE";
        } else {
            for (Threshold threshold : deviceThresholds) {
                String allowedStatus = threshold.getMatchExact();
                if (!actualStatus.equals(allowedStatus)) {
                    status = "ERROR";
                    eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                            device.getName() + ": " + parameter.getDescription() + " = " + actualStatus
                            + ". Допустимое значение [" + allowedStatus + "]"));
                    eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                            threshold.getParameter().getName() + ": " +
                            " = " + actualStatus + ". Допустимое значение [" + allowedStatus + "]"));
                    eventDispatcher.dispatch(EventMessage.db(EventType.DB, MessageType.ERROR, AlarmType.EVERYWHERE,
                            appSetting.getAlarmActive(), device.getPeriod(),
                            device.getName() + ": " + parameter.getDescription() + " = " + actualStatus));
                }
            }
        }

        return new ResultValue(actualStatus, status);
    }
}
