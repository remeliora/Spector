package com.example.spector.controller.rest.v0;

import com.example.spector.domain.DeviceType;
import com.example.spector.service.devicetype.DeviceTypeService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Hidden
@RestController
@RequestMapping("${application.endpoint.root}")
@RequiredArgsConstructor
public class DeviceTypeRestController {
    private DeviceTypeService deviceTypeService;

    @PostMapping("${application.endpoint.device-type}")
    public ResponseEntity<DeviceType> createDeviceType(@RequestBody DeviceType deviceType) {
        DeviceType createdDeviceType = deviceTypeService.createDeviceType(deviceType);
        return new ResponseEntity<>(createdDeviceType, HttpStatus.CREATED);
    }

    @GetMapping("${application.endpoint.device-type}")
    public ResponseEntity<Iterable<DeviceType>> getAllDeviceTypes() {
        Iterable<DeviceType> deviceTypes = deviceTypeService.getAllDeviceTypes();
        return new ResponseEntity<>(deviceTypes, HttpStatus.OK);
    }

    @GetMapping("${application.endpoint.device-type}/{deviceTypeId}")
    public ResponseEntity<DeviceType> getDeviceTypeById(@PathVariable Long deviceTypeId) {
        DeviceType deviceType = deviceTypeService.getDeviceTypeById(deviceTypeId);
        if (deviceType != null) {
            return new ResponseEntity<>(deviceType, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("${application.endpoint.device-type}/{deviceTypeId}")
    public ResponseEntity<DeviceType> updateDeviceType(@PathVariable Long deviceTypeId, @RequestBody DeviceType deviceType) {
        DeviceType updatedDeviceType = deviceTypeService.updateDeviceType(deviceTypeId, deviceType);
        if (updatedDeviceType != null) {
            return new ResponseEntity<>(updatedDeviceType, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("${application.endpoint.device-type}/{deviceTypeId}")
    public ResponseEntity<Void> deleteDeviceType(@PathVariable Long deviceTypeId) {
        deviceTypeService.deleteDeviceType(deviceTypeId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
