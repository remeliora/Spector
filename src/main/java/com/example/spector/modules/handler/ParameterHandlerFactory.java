package com.example.spector.modules.handler;

import com.example.spector.domain.enums.DataType;
import com.example.spector.domain.parameter.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParameterHandlerFactory {
    private final RegularParameterHandler regularParameterHandler;
    private final EnumeratedParameterHandler enumeratedParameterHandler;

    public ParameterHandler getParameterHandler(Parameter parameter) {
        DataType dataType = parameter.getDataType();
        return (dataType == DataType.ENUMERATED)
                ? enumeratedParameterHandler
                : regularParameterHandler;
    }
}
