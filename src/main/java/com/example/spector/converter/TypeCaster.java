package com.example.spector.converter;

import org.snmp4j.smi.Variable;

public interface TypeCaster<T> {
    T cast(Variable variable);
}
