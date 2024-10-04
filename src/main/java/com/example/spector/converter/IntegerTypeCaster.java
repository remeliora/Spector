package com.example.spector.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.smi.*;

public class IntegerTypeCaster implements TypeCaster<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(IntegerTypeCaster.class);

    @Override
    public Integer cast(Variable variable) {
        if (variable == null) {
            logger.error("Variable is empty");
            return null;
        }

        if (variable instanceof Integer32) {
            return ((Integer32) variable).toInt();
        } else if (variable instanceof Counter32) {
            return (int) ((Counter32) variable).getValue();
        } else if (variable instanceof Gauge32) {
            return (int) ((Gauge32) variable).getValue();
        } else if (variable instanceof Counter64) {
            return (int) ((Counter64) variable).getValue();
        } else if (variable instanceof UnsignedInteger32) {
            return (int) ((UnsignedInteger32) variable).toLong();
        } else {
            logger.error("Unsupported Variable type for integer casting: {} with value: {}",
                    variable.getClass().getSimpleName(), variable);
            throw new IllegalArgumentException("Unsupported Variable type for integer casting: " + variable.getClass().getSimpleName());
        }
    }
}
