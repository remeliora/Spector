package com.example.spector.checker.threshold;

import com.example.spector.domain.dto.ParameterDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ThresholdCheckerFactory {
//    private final StatusThresholdChecker statusThresholdChecker;
//    private final RegularThresholdChecker regularThresholdChecker;

    public static ThresholdChecker getThresholdChecker(ParameterDTO parameterDTO) {
        if (parameterDTO.getIsEnumeratedStatus()) {
            return new StatusThresholdChecker();
        } else {
            return new RegularThresholdChecker();
        }
//        return parameterDTO.getIsEnumeratedStatus() ? statusThresholdChecker : regularThresholdChecker;
    }
}
