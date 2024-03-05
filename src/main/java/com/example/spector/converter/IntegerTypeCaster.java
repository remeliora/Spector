package com.example.spector.converter;

import org.snmp4j.smi.*;

public class IntegerTypeCaster implements TypeCaster<Integer> {

    @Override
    public Integer cast(Variable variable) {
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
            throw new IllegalArgumentException("Unsupported Variable type for integer casting: " + variable.getClass().getSimpleName());
        }
    }
}
