package com.example.spector.domain;

import com.example.spector.domain.enums.DataType;
import jakarta.persistence.*;
import lombok.Data;

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
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "device_type_id", referencedColumnName = "id")
    private DeviceType deviceType;

    private String metric;

    private String description;

    @Enumerated(EnumType.STRING)
    private DataType dataType;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parameter", cascade = CascadeType.ALL)
    private Set<Threshold> thresholds;
}
