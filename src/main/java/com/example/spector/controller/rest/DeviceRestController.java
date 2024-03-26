package com.example.spector.controller.rest;

import com.example.spector.domain.Device;
import com.example.spector.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${application.endpoint.root}")
@RequiredArgsConstructor
public class DeviceRestController {
    @Autowired
    private DeviceService deviceService;

    @PostMapping("${application.endpoint.device}")
    public ResponseEntity<Device> createDevice(@RequestBody Device device) {
        Device createdDevice = deviceService.createDevice(device);
        return new ResponseEntity<>(createdDevice, HttpStatus.CREATED);
    }

    @GetMapping("${application.endpoint.device}")
    public ResponseEntity<Iterable<Device>> getAllDevices() {
        Iterable<Device> devices = deviceService.getAllDevices();
        return new ResponseEntity<>(devices, HttpStatus.OK);
    }

    @GetMapping("${application.endpoint.device}/{deviceId}")
    public ResponseEntity<Device> getDeviceById(@PathVariable Long deviceId) {
        Device device = deviceService.getDeviceById(deviceId);
        if (device != null) {
            return new ResponseEntity<>(device, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("${application.endpoint.device}/{deviceId}")
    public ResponseEntity<Device> updateDevice(@PathVariable Long deviceId, @RequestBody Device device) {
        Device updateDevice = deviceService.updateDevice(deviceId, device);
        if (updateDevice != null) {
            return new ResponseEntity<>(updateDevice, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("${application.endpoint.device}/{deviceId}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long deviceId) {
        deviceService.deleteDevice(deviceId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
