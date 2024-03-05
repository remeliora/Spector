package com.example.spector.domain;

import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Document
public class DeviceData {
    @Id
    private String id;

    private Long deviceId;

    private String deviceName;

    private String deviceIp;

    private LocalDateTime lastPollingTime;

    private Map<String, Object> parameters;
}
