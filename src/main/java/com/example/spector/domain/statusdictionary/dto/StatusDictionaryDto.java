package com.example.spector.domain.statusdictionary.dto;

import lombok.Value;

import java.util.Map;

/**
 * DTO for {@link com.example.spector.domain.statusdictionary.StatusDictionary}
 */
@Value
public class StatusDictionaryDto {
    Long id;
    String name;
    Map<Integer, String> enumValues;
}