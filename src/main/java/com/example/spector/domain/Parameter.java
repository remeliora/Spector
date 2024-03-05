package com.example.spector.domain;

import com.example.spector.domain.enums.DataType;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Objects;
import java.util.Set;

@Data
@Entity
@Table(name = "parameters")
public class Parameter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String address;

    // Связь с типом устройства
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_type_id", referencedColumnName = "id")
    private DeviceType deviceType;

    private String metric;

    private Double additive;

    private Double coefficient;

    private String description;

    @Enumerated(EnumType.STRING)
    private DataType dataType;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "parameter", cascade = CascadeType.ALL)
    private Set<Threshold> thresholds;

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
}
