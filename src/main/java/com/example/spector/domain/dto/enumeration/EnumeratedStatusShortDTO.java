package com.example.spector.domain.dto.enumeration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class EnumeratedStatusShortDTO {
    @NotBlank(message = "Enumerated Status is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @NotEmpty
    private Map<@NotNull Integer, @NotNull String> enumValues;
}
