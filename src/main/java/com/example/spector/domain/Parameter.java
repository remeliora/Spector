package com.example.spector.domain;

import com.example.spector.domain.enums.DataType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "parameters")
public class Parameter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String address;

    // Связь с типом устройства
    @JsonIdentityReference(alwaysAsId = true)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_type_id", referencedColumnName = "id")
    private DeviceType deviceType;

    private String metric;

    private Double additive;

    private Double coefficient;

    private Boolean isEnumeratedStatus;

    private String description;

    @Enumerated(EnumType.STRING)
    private DataType dataType;

    @JsonBackReference(value = "parameter")
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "parameter", cascade = CascadeType.ALL)
    private List<Threshold> thresholds;

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Parameter parameter = (Parameter) o;
        return Objects.equals(id, parameter.id) && Objects.equals(name, parameter.name);
    }
    @Override
    public String toString() {
        return name;
    }
}
