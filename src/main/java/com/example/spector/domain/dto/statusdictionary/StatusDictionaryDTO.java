package com.example.spector.domain.dto.statusdictionary;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class StatusDictionaryDTO {
    private Long id;
    private String name;
    private Map<Integer, String> enumValues;
}
