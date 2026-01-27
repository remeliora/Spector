package com.example.spector.domain.parameter;

import com.example.spector.domain.threshold.Threshold;
import com.example.spector.domain.devicetype.DeviceType;
import com.example.spector.domain.enums.DataType;
import com.example.spector.domain.override.DeviceParameterOverride;
import com.example.spector.domain.statusdictionary.StatusDictionary;
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

    private String description;

    @Enumerated(EnumType.STRING)
    private DataType dataType;

    @JsonIdentityReference(alwaysAsId = true)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_dictionary_id", referencedColumnName = "id")
    private StatusDictionary statusDictionary;

    @JsonBackReference(value = "parameter")
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parameter", cascade = CascadeType.ALL)
    private List<Threshold> thresholds;

    @JsonBackReference(value = "parameterOverride")
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parameter", cascade = CascadeType.ALL)
    private List<DeviceParameterOverride> deviceParameterOverrides;

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Parameter parameter = (Parameter) o;
        return Objects.equals(id, parameter.id);
    }

    @Override
    public String toString() {
        return name;
    }
}
