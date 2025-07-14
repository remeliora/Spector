package com.example.spector.domain.dto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class EnumDTO {
    private String name;
    private String displayName;

    public EnumDTO(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }
}


