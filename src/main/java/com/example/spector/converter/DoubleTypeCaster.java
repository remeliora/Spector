package com.example.spector.converter;

import org.snmp4j.smi.*;

public class DoubleTypeCaster implements TypeCaster<Double> {

    @Override
    public Double cast(Variable variable) {
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
            throw new IllegalArgumentException("Unsupported Variable type for double casting: " + variable.getClass().getSimpleName());
        }
    }
}
