package com.example.spector.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Objects;
import java.util.Set;

@Data
@Entity
@Table(name = "device_types")
public class DeviceType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "deviceType", cascade = CascadeType.ALL)
    private Set<Parameter> parameters;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "deviceType", cascade = CascadeType.ALL)
    private Set<Device> devices;

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceType that = (DeviceType) o;
        return Objects.equals(id, that.id);
    }
}
