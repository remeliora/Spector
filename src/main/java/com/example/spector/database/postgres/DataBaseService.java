package com.example.spector.database.postgres;

import com.example.spector.domain.*;
import com.example.spector.domain.dto.DeviceDTO;
import com.example.spector.domain.dto.DeviceTypeDTO;
import com.example.spector.domain.dto.ParameterDTO;
import com.example.spector.domain.dto.ThresholdDTO;
import com.example.spector.mapper.DeviceDTOConverter;
import com.example.spector.mapper.DeviceTypeDTOConverter;
import com.example.spector.mapper.ParameterDTOConverter;
import com.example.spector.mapper.ThresholdDTOConverter;
import com.example.spector.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataBaseService {  //Файл для работы с репозиториями сущностей БД
    private final DeviceTypeRepository deviceTypeRepository;
    private final DeviceTypeDTOConverter deviceTypeDTOConverter;
    private final DeviceRepository deviceRepository;
    private final DeviceDTOConverter deviceDTOConverter;
    private final ParameterRepository parameterRepository;
    private final ParameterDTOConverter parameterDTOConverter;
    private final ThresholdRepository thresholdRepository;
    private final ThresholdDTOConverter thresholdDTOConverter;

//    public List<DeviceType> getAllDeviceTypes() {
//        return deviceTypeRepository.findAll();
//    }

    public DeviceType getDeviceTypeById(Long id) {
        return deviceTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device Type not found"));
    }

    public DeviceTypeDTO convertDeviceTypeToDTO(DeviceType deviceType) {
        return deviceTypeDTOConverter.convertToDTO(deviceType);
    }

//    public List<Device> getAllDevices() {
//        return deviceRepository.findAll();
//    }

    public Device getDeviceById(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found"));
    }

    public DeviceDTO convertDeviceTypeToDTO(Device device) {
        return deviceDTOConverter.convertToDTO(device);
    }

//    public List<Parameter> getAllParameters() {
//        return parameterRepository.findAll();
//    }

    public Parameter getParameterById(Long id) {
        return parameterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parameter not found"));
    }

    public ParameterDTO convertParameterToDTO(Parameter parameter) {
        return parameterDTOConverter.convertToDTO(parameter);
    }

//    public List<Threshold> getAllThresholds() {
//        return thresholdRepository.findAll();
//    }

    public Threshold getThresholdById(Long id) {
        return thresholdRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Threshold not found"));
    }

    public ThresholdDTO convertThresholdToDTO(Threshold threshold) {
        return thresholdDTOConverter.convertToDTO(threshold);
    }

//    public Device getDeviceByName(String name) {
//        return deviceRepository.findByName(name);
//    }
//
//    public List<Device> getDeviceByIsEnableTrue() {
//        return deviceRepository.findDeviceByIsEnableTrue();
//    }

    public List<DeviceDTO> getDeviceDTOByIsEnableTrue() {
        return deviceRepository.findDeviceByIsEnableTrue().stream()
                .map(deviceDTOConverter::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<Parameter> getParameterByDeviceType(DeviceType deviceType) {
        return parameterRepository.findParameterByDeviceType(deviceType);
    }

    public List<ParameterDTO> getParameterDTOByDeviceType(DeviceType deviceType) {
        return parameterRepository.findParameterByDeviceType(deviceType).stream()
                .map(parameterDTOConverter::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<Threshold> getThresholdsByParameter(Parameter parameter) {
        return thresholdRepository.findThresholdByParameter(parameter);
    }

    public List<Threshold> getThresholdsByDevice(Device device) {
        return thresholdRepository.findThresholdByDevice(device);
    }

    public List<Threshold> getThresholdsByParameterAndIsEnableTrue(Parameter parameter) {
        return thresholdRepository.findThresholdByParameterAndIsEnableTrue(parameter);
    }

    public List<ThresholdDTO> getThresholdsByParameterDTOAndIsEnableTrue(ParameterDTO parameterDTO) {
        List<Threshold> thresholds = thresholdRepository.findThresholdByParameterIdAndAndIsEnableTrue(parameterDTO.getId());
        return thresholds.stream()
                .map(thresholdDTOConverter::convertToDTO)
                .collect(Collectors.toList());
    }

    public DeviceTypeDTO loadDeviceTypeWithParameters(Long deviceTypeId) {
        // Загружаем тип устройства
        DeviceType deviceType = getDeviceTypeById(deviceTypeId);

        // Преобразуем тип устройства в DTO
        DeviceTypeDTO deviceTypeDTO = convertDeviceTypeToDTO(deviceType);

        // Загружаем параметры этого типа устройства и преобразуем параметры в DTO
        List<ParameterDTO> parameterDTOList = getParameterDTOByDeviceType(deviceType);

        // Устанавливаем параметры в DTO типа устройства
        deviceTypeDTO.setParameter(parameterDTOList);

        return deviceTypeDTO;
    }
}
