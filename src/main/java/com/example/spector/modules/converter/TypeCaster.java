package com.example.spector.modules.converter;

import com.example.spector.domain.parameter.dto.ParameterDTO;
import org.snmp4j.smi.Variable;

public interface TypeCaster<T> {
    T cast(ParameterDTO parameterDTO, Variable variable);
}
