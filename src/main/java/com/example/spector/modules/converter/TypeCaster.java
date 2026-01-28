package com.example.spector.modules.converter;

import com.example.spector.domain.parameter.Parameter;
import org.snmp4j.smi.Variable;

public interface TypeCaster<T> {
    T cast(Parameter parameter, Variable variable);
}
