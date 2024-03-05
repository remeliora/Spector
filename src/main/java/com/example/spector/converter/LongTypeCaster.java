package com.example.spector.converter;

import org.snmp4j.smi.*;

public class LongTypeCaster implements TypeCaster<Long> {
    @Override
    public Long cast(Variable variable) {
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
            throw new IllegalArgumentException("Unsupported Variable type for integer casting: " + variable.getClass().getSimpleName());
        }
    }
}
