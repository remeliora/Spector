package com.example.spector.checker.threshold;

import com.example.spector.domain.dto.DeviceDTO;
import com.example.spector.domain.dto.ThresholdDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RegularThresholdChecker implements ThresholdChecker {
    private static final Logger logger = LoggerFactory.getLogger(RegularThresholdChecker.class);
    private static final Logger deviceLogger = LoggerFactory.getLogger("DeviceLogger");
    @Override
    public void checkThresholds(Object value, List<ThresholdDTO> thresholdDTOList, DeviceDTO deviceDTO) {
        for (ThresholdDTO thresholdDTO : thresholdDTOList) {
            if (thresholdDTO.getDevice().getId().equals(deviceDTO.getId())) {
                double lowValue = thresholdDTO.getLowValue();
                double highValue = thresholdDTO.getHighValue();

                if ((double) value < lowValue || (double) value > highValue) {
//                    System.out.println("Threshold crossed: Parameter " + thresholdDTO.getParameter().getName() +
//                            " with value " + processedValue + " is out of range [" + lowValue + ", " + highValue + "]");
                    logger.error("Threshold crossed: Parameter {} with value {} is out of range [{}, {}]",
                            thresholdDTO.getParameter().getName(), value, lowValue, highValue);
                    deviceLogger.error("Threshold crossed: Parameter {} with value {} is out of range [{}, {}]",
                            thresholdDTO.getParameter().getName(), value, lowValue, highValue);
                }
//                else {
//                    System.out.println("Threshold successful");
//                    logger.info("Threshold successful");
//                    deviceLogger.info("Threshold successful");
//                }
            }
        }
    }
}
