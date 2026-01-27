package com.example.spector.modules.converter;

import com.example.spector.domain.parameter.dto.ParameterDTO;
import com.example.spector.domain.enums.DataType;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.modules.event.EventMessage;
import lombok.RequiredArgsConstructor;
import org.snmp4j.smi.Variable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VariableCaster {
    private final TypeCasterFactory typeCasterFactory;
    private final EventDispatcher eventDispatcher;

    public Object convert(ParameterDTO parameterDTO, Variable variable) {
        DataType dataType = DataType.valueOf(parameterDTO.getDataType());
        TypeCaster<?> typeCaster = typeCasterFactory.getTypeCaster(dataType);
        return castTo(parameterDTO, dataType, variable, typeCaster);
    }

    private <T> T castTo(ParameterDTO parameterDTO, DataType dataType, Variable variable, TypeCaster<T> typeCaster) {
        if (variable == null) {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Значение null невозможно преобразовать в " + dataType));

            return null; // Может быть обработано в виде исключения или дефолтного значения
        }
        return typeCaster.cast(parameterDTO, variable);
    }
}
