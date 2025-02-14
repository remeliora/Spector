package com.example.spector.script;

import com.example.spector.converter.TypeCaster;
import com.example.spector.converter.TypeCasterFactory;
import com.example.spector.domain.dto.ParameterDTO;
import com.example.spector.domain.enums.DataType;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.event.EventDispatcher;
import com.example.spector.event.EventMessage;
import lombok.RequiredArgsConstructor;
import org.snmp4j.smi.Variable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SnmpVariableConverter {
    private final TypeCasterFactory typeCasterFactory;
    private final EventDispatcher eventDispatcher;

    public Object convert(ParameterDTO parameterDTO, Variable variable) {
        DataType dataType = DataType.valueOf(parameterDTO.getDataType());
        TypeCaster<?> typeCaster = typeCasterFactory.getTypeCaster(dataType);
        return castTo(dataType, variable, typeCaster);
    }

    private <T> T castTo(DataType dataType, Variable variable, TypeCaster<T> typeCaster) {
        if (variable == null) {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Значение null невозможно преобразовать в " + dataType));

            return null; // Может быть обработано в виде исключения или дефолтного значения
        }
        return typeCaster.cast(variable);
    }
}
