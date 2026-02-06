package com.example.spector.domain.statusdictionary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

/**
 * DTO for {@link com.example.spector.domain.statusdictionary.StatusDictionary}
 */
@Value
public class StatusDictionarySummaryDto {
    @NotNull
    Long id;
    @Size(max = 20)
    @NotBlank
    String name;
    Integer count;
}