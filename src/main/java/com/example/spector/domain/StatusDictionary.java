package com.example.spector.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Entity
@Table (name = "status_dictionaries")
@Getter
@Setter
public class StatusDictionary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Type(JsonBinaryType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<Integer, String> enumValues;

    @JsonBackReference(value = "parameterStatusDictionary")
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "statusDictionary")
    private List<Parameter> parameters;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatusDictionary statusDictionary = (StatusDictionary) o;
        return Objects.equals(id, statusDictionary.id);
    }

    @Override
    public String toString() {
        return name;
    }
}
