package com.example.spector.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.smi.*;

public class DoubleTypeCaster implements TypeCaster<Double> {
    private static final Logger logger = LoggerFactory.getLogger(DoubleTypeCaster.class);

    @Override
    public Double cast(Variable variable) {
        if (variable == null) {
            logger.error("Variable is empty");
            return null;
        }

        if (variable instanceof Integer32) {
            return (double) ((Integer32) variable).toInt();
        } else if (variable instanceof Counter32) {
            return (double) ((Counter32) variable).getValue();
        } else if (variable instanceof Gauge32) {
            return (double) ((Gauge32) variable).getValue();
        } else if (variable instanceof Counter64) {
            return (double) ((Counter64) variable).getValue();
        } else if (variable instanceof UnsignedInteger32) {
            return (double) ((UnsignedInteger32) variable).toLong();
        } else {
            logger.error("Unsupported Variable type for double casting: {}", variable.getClass().getSimpleName());
            throw new IllegalArgumentException("Unsupported Variable type for double casting: " + variable.getClass().getSimpleName());
        }
    }
}
