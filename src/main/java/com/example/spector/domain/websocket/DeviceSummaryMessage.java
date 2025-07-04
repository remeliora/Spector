package com.example.spector.domain.websocket;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DeviceSummaryMessage {
    private Long deviceId;
    private String status;
    private Boolean isEnable;
}
