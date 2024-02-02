package com.example.spector.service;

import com.example.spector.domain.Device;
import com.example.spector.domain.DeviceType;
import com.example.spector.domain.Parameter;
import com.example.spector.domain.Threshold;
import com.example.spector.repositories.DeviceRepository;
import com.example.spector.repositories.DeviceTypeRepository;
import com.example.spector.repositories.ParameterRepository;
import com.example.spector.repositories.ThresholdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataBaseService {  //Файл для работы с репозиториями сущностей БД
    private final DeviceTypeRepository deviceTypeRepository;
    private final DeviceRepository deviceRepository;
    private final ParameterRepository parameterRepository;
    private final ThresholdRepository thresholdRepository;

    @Autowired
    public DataBaseService(DeviceTypeRepository deviceTypeRepository,
                              DeviceRepository deviceRepository,
                              ParameterRepository parameterRepository,
                              ThresholdRepository thresholdRepository) {
        this.deviceTypeRepository = deviceTypeRepository;
        this.deviceRepository = deviceRepository;
        this.parameterRepository = parameterRepository;
        this.thresholdRepository = thresholdRepository;
    }

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

    public Iterable<Parameter> getParameterByDeviceType(DeviceType deviceType) {
        return parameterRepository.findParameterByDeviceType(deviceType);
    }

    public Iterable<Threshold> getThresholdsByParameter(Parameter parameter) {
        return thresholdRepository.findThresholdByParameter(parameter);
    }

    public Iterable<Threshold> getThresholdsByDevice(Device device) {
        return thresholdRepository.findThresholdByDevice(device);
    }
}
