package com.example.spector.domain.websocket;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DeviceDataStatusDTO {
    private Long deviceId;
    private String status;
    private Boolean isEnable;
}
