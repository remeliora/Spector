package com.example.spector.service.parameter;

import com.example.spector.domain.DeviceType;
import com.example.spector.domain.Parameter;
import com.example.spector.domain.dto.parameter.rest.ParameterBaseDTO;
import com.example.spector.domain.dto.parameter.rest.ParameterCreateDTO;
import com.example.spector.domain.dto.parameter.rest.ParameterDetailDTO;
import com.example.spector.mapper.BaseDTOConverter;
import com.example.spector.repositories.DeviceTypeRepository;
import com.example.spector.repositories.ParameterRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AggregationParameterService {
    private final ParameterRepository parameterRepository;
    private final BaseDTOConverter baseDTOConverter;
    private final DeviceTypeRepository deviceTypeRepository;

    // Получение списка с фильтрацией
    public List<ParameterBaseDTO> getParameterByDeviceType(Long deviceTypeId) {
        DeviceType deviceType = deviceTypeRepository.findById(deviceTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Device type not found with id: " + deviceTypeId));

        return parameterRepository.findParameterByDeviceType(deviceType).stream()
                .map(parameter -> baseDTOConverter.toDTO(parameter, ParameterBaseDTO.class))
                .toList();
    }

    // Получение деталей параметра
    public ParameterDetailDTO getParameterDetails(Long deviceTypeId, Long parameterId) {
        Parameter parameter = parameterRepository.findParameterByIdAndDeviceTypeId(parameterId, deviceTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Parameter not found with id: " + parameterId +
                                                               " for device type: " + deviceTypeId));

        return baseDTOConverter.toDTO(parameter, ParameterDetailDTO.class);
    }

    //================
    //      CRUD
    //================

    // Создание нового параметр
    @Transactional
    public ParameterDetailDTO createParameter(Long deviceTypeId, ParameterCreateDTO createDTO) {
        DeviceType deviceType = deviceTypeRepository.findById(deviceTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Device type not found with id: " + deviceTypeId));

        Parameter newParameter = baseDTOConverter.toEntity(createDTO, Parameter.class);
        newParameter.setDeviceType(deviceType);

        Parameter savedParameter = parameterRepository.save(newParameter);

        return baseDTOConverter.toDTO(savedParameter, ParameterDetailDTO.class);
    }

    // Обновление параметра
    @Transactional
    public ParameterDetailDTO updateParameter(Long deviceTypeId, Long parameterId,
                                               ParameterDetailDTO updateDTO) {
        if (!parameterId.equals(updateDTO.getId())) {
            throw new IllegalArgumentException("ID in path and body must match");
        }

        Parameter existingParameter = parameterRepository.findParameterByIdAndDeviceTypeId(parameterId, deviceTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Parameter not found with id: " + parameterId +
                                                               " for device type: " + deviceTypeId));

        existingParameter.setName(updateDTO.getName());
        existingParameter.setAddress(updateDTO.getAddress());
        existingParameter.setMetric(updateDTO.getMetric());
        existingParameter.setAdditive(updateDTO.getAdditive());
        existingParameter.setCoefficient(updateDTO.getCoefficient());
        existingParameter.setIsEnumeratedStatus(updateDTO.getIsEnumeratedStatus());
        existingParameter.setDescription(updateDTO.getDescription());
        existingParameter.setDataType(updateDTO.getDataType());

        Parameter updatedParameter = parameterRepository.save(existingParameter);

        return baseDTOConverter.toDTO(updatedParameter, ParameterDetailDTO.class);
    }

    // Удаление параметра
    @Transactional
    public void deleteParameter(Long deviceTypeId, Long parameterId) {
        if (!parameterRepository.existsParameterByIdAndDeviceTypeId(parameterId, deviceTypeId)) {
            throw new EntityNotFoundException("Parameter not found with id: " + parameterId +
                                              " for device type: " + deviceTypeId);
        }
        parameterRepository.deleteById(parameterId);
    }

}
