package com.example.spector.domain;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Document
public class DeviceData {
    @Id
    private String id;

    private Long deviceId;

    private String deviceName;

    private String deviceIp;

    private String location;

    private String status;

    private LocalDateTime lastPollingTime;

    private List<ParameterData> parameters;
}
