package com.example.spector.domain.device.dto;

import com.example.spector.domain.enums.AlarmType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class DeviceUpdateDTO {
    @NotNull(message = "ID is required for update")
    private Long id;

    @NotBlank(message = "Device name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "IP address is required")
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
    @Min(value = 15, message = "Period must be at least 15 seconds")
    @Max(value = 60, message = "Period cannot exceed 60 seconds")
    private Integer period;

    @NotNull(message = "Alarm Type is required")
    private AlarmType alarmType;

    @NotNull(message = "Is Enable status is required")
    private Boolean isEnable;

    private List<Long> activeParametersId;
}
