package com.example.spector.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "values")
public class Value {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long valueId;

    private String oidNumber;

    private String oidValue;

    // Связь с параметром
    @OneToOne
    @JoinColumn(name = "parameter_id", referencedColumnName = "parameterId")
    private Parameter parameter;

    // Связь с устройством
    @ManyToOne
    private Device device;

    // Связь с типом устройства
    @ManyToOne
    private DeviceType deviceType;
}
