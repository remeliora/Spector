package com.example.spector.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "thresholds")
public class Threshold {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double lowValue;

    private Double highValue;

    private Double additive;

    private Double coefficient;

    private Boolean isEnable;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "parameter_id", referencedColumnName = "id")
    private Parameter parameter;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "device_id", referencedColumnName = "id")
    private Device device;
}
