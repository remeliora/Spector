package com.example.spector.domain.dto.device.rest;

import com.example.spector.domain.enums.AlarmType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DeviceCreateDTO {
    @NotBlank(message = "Device is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "IP is required")
    @Pattern(regexp = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$",
            message = "Invalid IP address format")
    private String ipAddress;

    @NotNull(message = "Device type ID is required")
    private Long deviceTypeId;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Size(max = 100, message = "Location cannot exceed 100 characters")
    private String location;

    @NotNull(message = "Period is required")
    @Min(value = 15, message = "Period must be at least 15")
    @Max(value = 60, message = "Period cannot exceed 60")
    private Integer period;

    @NotNull(message = "Alarm Type is required")
    private AlarmType alarmType;

    @NotNull(message = "Is Enable is required")
    private Boolean isEnable;

    private List<Long> activeParametersId;
}
