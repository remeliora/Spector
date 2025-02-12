package com.example.spector.checker.threshold;

import com.example.spector.domain.dto.ParameterDTO;
import org.springframework.stereotype.Component;

@Component
public class ThresholdCheckerFactory {
    public static ThresholdChecker getThresholdChecker(ParameterDTO parameterDTO) {
        if (parameterDTO.getIsEnumeratedStatus()) {
            return new StatusThresholdChecker();
        } else {
            return new RegularThresholdChecker();
        }
    }
}
