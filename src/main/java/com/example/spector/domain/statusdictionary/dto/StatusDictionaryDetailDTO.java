package com.example.spector.domain.statusdictionary.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class StatusDictionaryDetailDTO {
    private Long id;

    private String name;

    private Map<Integer, String> enumValues;
}
