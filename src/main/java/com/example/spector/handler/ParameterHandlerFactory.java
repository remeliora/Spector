package com.example.spector.handler;

import com.example.spector.domain.dto.ParameterDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParameterHandlerFactory {
    private final RegularParameterHandler regularParameterHandler;
    private final EnumeratedParameterHandler enumeratedParameterHandler;

    public ParameterHandler getParameterHandler(ParameterDTO parameterDTO) {
        if (parameterDTO.getIsEnumeratedStatus()) {
            return enumeratedParameterHandler;
        } else {
            return regularParameterHandler;
        }
    }
}
