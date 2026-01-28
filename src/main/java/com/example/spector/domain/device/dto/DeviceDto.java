package com.example.spector.domain.device.dto;

import com.example.spector.domain.device.Device;
import com.example.spector.domain.enums.AlarmType;
import jakarta.validation.constraints.*;
import lombok.Value;

import java.util.List;

/**
 * DTO for {@link Device}
 */
@Value
public class DeviceDto {
    @NotNull
    Long id;

    @Size(max = 100)
    @NotBlank
    String name;

    @Pattern(regexp = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")
    @NotBlank
    String ipAddress;

    @NotNull
    Long deviceTypeId;

    @Size(max = 500)
    String description;

    @Size(max = 100)
    String location;

    @NotNull
    @Min(15)
    @Max(60)
    Integer period;

    @NotNull
    AlarmType alarmType;

    @NotNull
    Boolean isEnable;

    List<Long> activeParametersId;
}