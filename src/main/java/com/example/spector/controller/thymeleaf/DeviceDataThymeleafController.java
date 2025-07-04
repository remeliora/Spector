package com.example.spector.controller.thymeleaf;

import com.example.spector.domain.Device;
import com.example.spector.domain.DeviceType;
import com.example.spector.domain.Parameter;
import com.example.spector.domain.dto.device.DeviceDTO;
import com.example.spector.domain.dto.devicedata.DeviceDataDTO;
import com.example.spector.domain.dto.parameter.ParameterDTO;
import com.example.spector.mapper.DeviceDTOConverter;
import com.example.spector.mapper.ParameterDTOConverter;
import com.example.spector.database.mongodb.DeviceDataService;
import com.example.spector.service.device.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/devices-data")
public class DeviceDataThymeleafController {
    private final DeviceService deviceService;
    private final DeviceDataService deviceDataService;
    private final DeviceDTOConverter deviceDTOConverter;
    private final ParameterDTOConverter parameterDTOConverter;

    @GetMapping("/list")
    public String getAllDevicesAndParameters(Model model) {
        // Получаем список всех устройств
        List<Device> devices = deviceService.getAllDevices();
        // Преобразуем устройства в DTO
        List<DeviceDTO> deviceDTOs = new ArrayList<>();
        devices.forEach(device -> deviceDTOs.add(deviceDTOConverter.convertToDTO(device)));
        // Передаем список устройств на страницу
        model.addAttribute("devices", deviceDTOs);

        // Получаем параметры для каждого устройства и передаем их на страницу
        List<List<ParameterDTO>> parametersList = new ArrayList<>();
        deviceDTOs.forEach(deviceDTO -> {
            Long deviceId = deviceDTO.getId();
            List<ParameterDTO> parameters = getDeviceParameters(deviceId);
            parametersList.add(parameters);
        });
        model.addAttribute("deviceParameters", parametersList);

        // Получаем значения параметров для каждого устройства и передаем их на страницу
        Map<String, Object> parameterValues = new HashMap<>();
        deviceDTOs.forEach(deviceDTO -> {
            String deviceName = deviceDTO.getName();
            List<DeviceDataDTO> deviceDataDTOList = deviceDataService.getParametersByDeviceName(deviceName);
            deviceDataDTOList.forEach(deviceDataDTO -> {
                Map<String, Object> parameters = deviceDataDTO.getParameters();
                parameters.forEach((key, value) -> {
                    String parameterKey = deviceDataDTO.getDeviceName() + "-" + key;
                    parameterValues.put(parameterKey, value);
                });
            });
        });
        model.addAttribute("parameterValues", parameterValues);

        return "devicesdata/devicedata_list";
    }

    private List<ParameterDTO> getDeviceParameters(Long deviceId) {
        Device device = deviceService.getDeviceById(deviceId);
        if (device != null) {
            // Получаем тип устройства данного устройства
            DeviceType deviceType = device.getDeviceType();
            // Получаем список параметров по типу устройства
            List<Parameter> parameters = deviceType.getParameters();
            // Преобразуем параметры в DTO
            List<ParameterDTO> parameterDTOs = new ArrayList<>();
            parameters.forEach(parameter -> parameterDTOs.add(parameterDTOConverter.convertToDTO(parameter)));
            return parameterDTOs;
        }
        return new ArrayList<>();
    }

    @GetMapping("/values")
    @ResponseBody
    public Map<String, Object> getParameterValues() {
        Map<String, Object> parameterValues = new HashMap<>();
        List<Device> devices = deviceService.getAllDevices();
        devices.forEach(device -> {
            String deviceName = device.getName();
            List<DeviceDataDTO> deviceDataDTOList = deviceDataService.getParametersByDeviceName(deviceName);
            deviceDataDTOList.forEach(deviceDataDTO -> {
                Map<String, Object> parameters = deviceDataDTO.getParameters();
                parameters.forEach((key, value) -> {
                    String parameterKey = deviceDataDTO.getDeviceName() + "-" + key;
                    parameterValues.put(parameterKey, value);
                });
            });
        });
        return parameterValues;
    }
}
