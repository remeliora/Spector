package com.example.spector.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Data
@Entity
@Table(name = "devices")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deviceId;

    private String deviceName;

    private String ipAddress;

    // Связь с типом устройства
    @ManyToOne
    private DeviceType deviceType;

    // Связь с параметрами через тип устройства
    @ManyToMany
    @JoinTable(
            name = "device_parameters",
            joinColumns = @JoinColumn(name = "device_id"),
            inverseJoinColumns = @JoinColumn(name = "parameter_id")
    )
    private Set<Parameter> parameters;
}
