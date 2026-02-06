package com.example.spector.domain.statusdictionary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.util.Map;

/**
 * DTO for {@link com.example.spector.domain.statusdictionary.StatusDictionary}
 */
@Value
public class StatusDictionaryCreateDtoV1 {
    @Size(max = 20)
    @NotBlank
    String name;
    @NotNull
    Map<Integer, String> enumValues;
}