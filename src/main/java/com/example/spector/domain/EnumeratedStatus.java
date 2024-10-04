package com.example.spector.domain;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Getter
@Setter
@Document
public class EnumeratedStatus {
    @Id
    private String id;

    private String name;  // Имя списка перечислений

    private Map<Integer, String> enumValues;  // Ключи и значения перечислений
}
