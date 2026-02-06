package com.example.spector.service;

import com.example.spector.domain.devicetype.DeviceType;
import com.example.spector.domain.devicetype.dto.DeviceTypeCreateDtoV1;
import com.example.spector.domain.devicetype.dto.DeviceTypeDto;
import com.example.spector.mapper.DeviceTypeMapper;
import com.example.spector.repositories.DeviceTypeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class DeviceTypeServiceV1 {

    private final DeviceTypeMapper deviceTypeMapper;
    private final DeviceTypeRepository deviceTypeRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public DeviceTypeDto getOne(Long id) {
        Optional<DeviceType> deviceTypeOptional = deviceTypeRepository.findById(id);
        return deviceTypeMapper.toDeviceTypeDto(deviceTypeOptional.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id))));
    }

    @Transactional(readOnly = true)
    public List<DeviceTypeDto> getAll() {
        List<DeviceType> deviceTypes = deviceTypeRepository.findAll();
        return deviceTypes.stream()
                .map(deviceTypeMapper::toDeviceTypeDto)
                .toList();
    }

    @Transactional
    public DeviceTypeDto create(DeviceTypeCreateDtoV1 dto) {
        if (deviceTypeRepository.findByName(dto.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Device type with name '%s' already exists"
                    .formatted(dto.getName()));
        }
        DeviceType deviceType = deviceTypeMapper.toEntity(dto);
        DeviceType resultDeviceType = deviceTypeRepository.save(deviceType);
        return deviceTypeMapper.toDeviceTypeDto(resultDeviceType);
    }

    @Transactional
    public DeviceTypeDto update(Long id, DeviceTypeDto dto) {
        DeviceType deviceType = deviceTypeRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));
        if (!dto.getName().equals(deviceType.getName()) &&
           deviceTypeRepository.existsByName(dto.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Device type with name '%s' already exists"
                    .formatted(dto.getName()));
        }
        deviceTypeMapper.updateWithNull(dto, deviceType);
        DeviceType resultDeviceType = deviceTypeRepository.save(deviceType);
        return deviceTypeMapper.toDeviceTypeDto(resultDeviceType);
    }

    @Transactional
    public DeviceTypeDto patch(Long id, JsonNode patchNode) throws IOException {
        DeviceType deviceType = deviceTypeRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));

        DeviceTypeDto deviceTypeDto = deviceTypeMapper.toDeviceTypeDto(deviceType);
        objectMapper.readerForUpdating(deviceTypeDto).readValue(patchNode);
        deviceTypeMapper.updateWithNull(deviceTypeDto, deviceType);

        DeviceType resultDeviceType = deviceTypeRepository.save(deviceType);
        return deviceTypeMapper.toDeviceTypeDto(resultDeviceType);
    }

    @Transactional
    public void delete(Long id) {
        DeviceType deviceType = deviceTypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found"
                        .formatted(id)));
        if (deviceType != null) {
            deviceTypeRepository.delete(deviceType);
        }
    }
}
