package com.example.spector.modules.datapattern;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ResultValue {
    private Object value;

    private String status;
}
