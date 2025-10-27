package com.example.spector.modules.handler;

import com.example.spector.domain.dto.parameter.ParameterDTO;
import com.example.spector.domain.enums.DataType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParameterHandlerFactory {
    private final RegularParameterHandler regularParameterHandler;
    private final EnumeratedParameterHandler enumeratedParameterHandler;

    public ParameterHandler getParameterHandler(ParameterDTO parameterDTO) {
        DataType dataType = DataType.valueOf(parameterDTO.getDataType());
        return (dataType == DataType.ENUMERATED)
                ? enumeratedParameterHandler
                : regularParameterHandler;
    }
}
