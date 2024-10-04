package com.example.spector.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.smi.*;

public class LongTypeCaster implements TypeCaster<Long> {
    private static final Logger logger = LoggerFactory.getLogger(LongTypeCaster.class);
    @Override
    public Long cast(Variable variable) {
        if (variable == null) {
            logger.error("Variable is empty");
            return null;
        }

        if (variable instanceof Integer32) {
            return (long) ((Integer32) variable).toInt();
        } else if (variable instanceof Counter32) {
            return (long) ((Counter32) variable).getValue();
        } else if (variable instanceof Gauge32) {
            return (long) ((Gauge32) variable).getValue();
        } else if (variable instanceof Counter64) {
            return (long) ((Counter64) variable).getValue();
        } else if (variable instanceof UnsignedInteger32) {
            return (long) ((UnsignedInteger32) variable).toLong();
        } else {
            logger.error("Unsupported Variable type for long casting: {} with value: {}",
                    variable.getClass().getSimpleName(), variable);
            throw new IllegalArgumentException("Unsupported Variable type for long casting: " + variable.getClass().getSimpleName());
        }
    }
}
