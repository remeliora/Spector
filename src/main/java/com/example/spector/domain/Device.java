    package com.example.spector.domain;

    import com.example.spector.domain.enums.AlarmType;
    import com.fasterxml.jackson.annotation.JsonBackReference;
    import com.fasterxml.jackson.annotation.JsonIdentityInfo;
    import com.fasterxml.jackson.annotation.JsonIdentityReference;
    import com.fasterxml.jackson.annotation.ObjectIdGenerators;
    import jakarta.persistence.*;
    import lombok.Getter;
    import lombok.Setter;

    import java.util.Objects;
    import java.util.Set;

    @Getter
    @Setter
    @Entity
    @Table(name = "devices")
    public class Device {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;

        private String ipAddress;

        // Связь с типом устройства
        @JsonIdentityReference(alwaysAsId = true)
        @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "device_type_id", referencedColumnName = "id", nullable = true)
        private DeviceType deviceType;

        private String description;

        private Integer period;

        @Enumerated(EnumType.STRING)
        private AlarmType alarmType;

        private Boolean isEnable;

        @JsonBackReference(value = "device")
        @OneToMany(fetch = FetchType.EAGER, mappedBy = "device", cascade = CascadeType.ALL)
        private Set<Threshold> threshold;

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Device device = (Device) o;
            return Objects.equals(id, device.id);
        }

        @Override
        public String toString() {
            return name;
        }
    }
