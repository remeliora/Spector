package com.example.spector.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Objects;

@Data
@Entity
@Table(name = "thresholds")
public class Threshold {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double lowValue;

    private Double highValue;

    private Boolean isEnable;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parameter_id", referencedColumnName = "id")
    private Parameter parameter;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_id", referencedColumnName = "id")
    private Device device;

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Threshold threshold = (Threshold) o;
        return Objects.equals(id, threshold.id);
    }
}
