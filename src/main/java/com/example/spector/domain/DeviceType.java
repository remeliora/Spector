package com.example.spector.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Data
@Entity
@Table(name = "typesOfDev")
public class DeviceType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deviceTypeId;

    private String typeName;

    @OneToMany(mappedBy = "deviceType")
    private Set<Parameter> parameters;

    @OneToMany(mappedBy = "deviceType")
    private Set<Device> devices;
}
