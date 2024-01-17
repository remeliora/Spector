package com.example.spector.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "oidParameters")
public class Parameter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long parameterId;

    private String oidNumber;

    private String oidName;

    private String oidValue;

    // Связь с типом устройства
    @ManyToOne
    @JoinColumn(name = "device_type_id", referencedColumnName = "deviceTypeId")
    private DeviceType deviceType;

    // Связь с устройством
    @ManyToOne
    private Device device;

    // Связь со значениями
    @OneToOne(mappedBy = "parameter")
    private Value value;

    private String metric;

    private int controlLevel;

    private String description;
}
