package com.example.spector.checker.threshold;

import com.example.spector.domain.dto.DeviceDTO;
import com.example.spector.domain.dto.ThresholdDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StatusThresholdChecker implements ThresholdChecker {
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
                    deviceLogger.error("Threshold crossed: Parameter {} with value {} is out of range [{}]",
                            thresholdDTO.getParameter().getName(), value, matchExact);
                }
            }
        }
    }
}
