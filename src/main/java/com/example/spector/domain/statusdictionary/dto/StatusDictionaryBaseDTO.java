package com.example.spector.domain.statusdictionary.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class StatusDictionaryBaseDTO {
    private Long id;

    private String name;

    private Integer count;
}
