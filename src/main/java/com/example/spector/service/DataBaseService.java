package com.example.spector.service;

import com.example.spector.domain.*;
import com.example.spector.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DataBaseService {  //Файл для работы с репозиториями сущностей БД
    private final DeviceTypeRepository deviceTypeRepository;
    private final DeviceRepository deviceRepository;
    private final ParameterRepository parameterRepository;
    private final ThresholdRepository thresholdRepository;
    private final DeviceDataRepository deviceDataRepository;

    public Iterable<DeviceType> getAllDeviceTypes() {
        return deviceTypeRepository.findAll();
    }

    public Iterable<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public Iterable<Parameter> getAllParameters() {
        return parameterRepository.findAll();
    }

    public Iterable<Threshold> getAllThresholds() {
        return thresholdRepository.findAll();
    }

    public Device getDeviceByName(String name) {
        return deviceRepository.findByName(name);
    }

    public Iterable<Device> getDeviceByIsEnableTrue() {
        return deviceRepository.findDeviceByIsEnableTrue();
    }

    public Iterable<Parameter> getParameterByDeviceType(DeviceType deviceType) {
        return parameterRepository.findParameterByDeviceType(deviceType);
    }

    public Iterable<Threshold> getThresholdsByParameter(Parameter parameter) {
        return thresholdRepository.findThresholdByParameter(parameter);
    }

    public Iterable<Threshold> getThresholdsByDevice(Device device) {
        return thresholdRepository.findThresholdByDevice(device);
    }

    public Iterable<Threshold> getThresholdsByParameterAndIsEnableTrue(Parameter parameter) {
        return thresholdRepository.findThresholdByParameterAndIsEnableTrue(parameter);
    }

    public Iterable<DeviceData> getDeviceDataByDeviceName(String deviceName) {
        return deviceDataRepository.findByDeviceName(deviceName);
    }

    public boolean existsDeviceDataByDeviceName(String deviceName) {
        return deviceDataRepository.existsByDeviceName(deviceName);
    }
}
