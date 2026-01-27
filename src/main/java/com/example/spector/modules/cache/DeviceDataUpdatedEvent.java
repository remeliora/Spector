package com.example.spector.modules.cache;

import com.example.spector.domain.devicedata.dto.DeviceCurrentData;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class DeviceDataUpdatedEvent extends ApplicationEvent {
    private final Long deviceId;
    private final DeviceCurrentData currentData;

    public DeviceDataUpdatedEvent(Object source, Long deviceId, DeviceCurrentData currentData) {
        super(source);
        this.deviceId = deviceId;
        this.currentData = currentData;
    }
}
