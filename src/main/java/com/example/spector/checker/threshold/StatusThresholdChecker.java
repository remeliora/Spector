package com.example.spector.checker.threshold;

import com.example.spector.domain.dto.DeviceDTO;
import com.example.spector.domain.dto.ThresholdDTO;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.event.EventDispatcher;
import com.example.spector.event.EventMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StatusThresholdChecker implements ThresholdChecker {
//    private final EventDispatcher eventDispatcher;
    private static final Logger logger = LoggerFactory.getLogger(StatusThresholdChecker.class);
    private static final Logger deviceLogger = LoggerFactory.getLogger("DeviceLogger");
    @Override
    public void checkThresholds(Object value, List<ThresholdDTO> thresholdDTOList, DeviceDTO deviceDTO) {
        Integer intValue = (Integer) value;
        for (ThresholdDTO thresholdDTO : thresholdDTOList) {
            if (thresholdDTO.getDevice().getId().equals(deviceDTO.getId())) {
                int matchExact = thresholdDTO.getMatchExact();

                if (matchExact == intValue) {
                    // Значение соответствует порогу
//                    deviceLogger.info("Threshold successful");
                    return;
                } else {
                    logger.error("Threshold crossed: Parameter {} with value {} is out of range [{}]",
                            thresholdDTO.getParameter().getName(), value, matchExact);
//                    eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
//                            "Нарушение порога: " + thresholdDTO.getParameter().getName() +
//                                    " = " + value + ". Допустимое значение [" + matchExact + "]"));
                    deviceLogger.error("Threshold crossed: Parameter {} with value {} is out of range [{}]",
                            thresholdDTO.getParameter().getName(), value, matchExact);
//                    eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
//                            "Нарушение порога: " + thresholdDTO.getParameter().getName() +
//                                    " = " + value + ". Допустимое значение [" + matchExact + "]"));
                }
            }
        }
    }
}
