package com.example.spector.converter;

import org.snmp4j.smi.Variable;

public class StringTypeCaster implements TypeCaster<String> {
    @Override
    public String cast(Variable variable) {
        return variable.toString();
    }
}
