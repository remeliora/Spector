package com.example.spector.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "device_types")
public class DeviceType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String className;

    private String description;

    @JsonBackReference(value = "parameterDeviceType")
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "deviceType", cascade = CascadeType.ALL/*, orphanRemoval = true*/)
    /*@OnDelete(action = OnDeleteAction.CASCADE)*/
    private List<Parameter> parameters;

    @JsonBackReference(value = "deviceDeviceType")
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "deviceType", cascade = CascadeType.ALL/*, orphanRemoval = true*/)
    /*@OnDelete(action = OnDeleteAction.CASCADE)*/
    private List<Device> devices;

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

    @Override
    public String toString() {
        return name;
    }
}
