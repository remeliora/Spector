package com.example.spector.domain;

import com.example.spector.domain.enums.AlarmType;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Data
@Entity
@Table(name = "devices")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String ipAddress;

    // Связь с типом устройства
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "device_type_id", referencedColumnName = "id")
    private DeviceType deviceType;

    private String description;

    private Integer period;

    @Enumerated(EnumType.STRING)
    private AlarmType alarmType;

    private Boolean isEnable;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "device", cascade = CascadeType.ALL)
    private Set<Threshold> threshold;
}
