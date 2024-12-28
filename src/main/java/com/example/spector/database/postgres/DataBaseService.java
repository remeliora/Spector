package com.example.spector.database.postgres;

import com.example.spector.domain.DeviceType;
import com.example.spector.domain.Threshold;
import com.example.spector.domain.dto.DeviceDTO;
import com.example.spector.domain.dto.DeviceTypeDTO;
import com.example.spector.domain.dto.ParameterDTO;
import com.example.spector.domain.dto.ThresholdDTO;
import com.example.spector.mapper.DeviceDTOConverter;
import com.example.spector.mapper.DeviceTypeDTOConverter;
import com.example.spector.mapper.ParameterDTOConverter;
import com.example.spector.mapper.ThresholdDTOConverter;
import com.example.spector.repositories.DeviceRepository;
import com.example.spector.repositories.DeviceTypeRepository;
import com.example.spector.repositories.ParameterRepository;
import com.example.spector.repositories.ThresholdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public DeviceType getDeviceTypeById(Long id) {
        return deviceTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device Type not found"));
    }

    public DeviceTypeDTO convertDeviceTypeToDTO(DeviceType deviceType) {
        return deviceTypeDTOConverter.convertToDTO(deviceType);
    }

    public List<DeviceDTO> getDeviceDTOByIsEnableTrue() {
        return deviceRepository.findDeviceByIsEnableTrue().stream()
                .map(deviceDTOConverter::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ParameterDTO> getParameterDTOByDeviceType(DeviceType deviceType) {
        return parameterRepository.findParameterByDeviceType(deviceType).stream()
                .map(parameterDTOConverter::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ThresholdDTO> getThresholdsByParameterDTOAndIsEnableTrue(ParameterDTO parameterDTO) {
        List<Threshold> thresholds = thresholdRepository.findThresholdByParameterIdAndIsEnableTrue(parameterDTO.getId());
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
