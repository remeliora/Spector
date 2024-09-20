package com.example.spector.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.smi.Variable;

public class StringTypeCaster implements TypeCaster<String> {
    private static final Logger logger = LoggerFactory.getLogger(StringTypeCaster.class);
    @Override
    public String cast(Variable variable) {
        if (variable == null) {
            logger.error("Variable is empty");
            return null;
        }

        return variable.toString();
    }
}
