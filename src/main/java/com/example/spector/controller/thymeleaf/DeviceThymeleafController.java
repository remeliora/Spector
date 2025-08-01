package com.example.spector.controller.thymeleaf;

import com.example.spector.domain.Device;
import com.example.spector.domain.DeviceType;
import com.example.spector.domain.dto.device.DeviceDTO;
import com.example.spector.domain.dto.devicetype.DeviceTypeDTO;
import com.example.spector.mapper.DeviceDTOConverter;
import com.example.spector.mapper.DeviceTypeDTOConverter;
import com.example.spector.service.device.DeviceService;
import com.example.spector.service.devicetype.DeviceTypeService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Hidden
@Controller
@RequiredArgsConstructor
@RequestMapping("/devices")
public class DeviceThymeleafController {
    private final DeviceService deviceService;
    private final DeviceTypeService deviceTypeService;
    private final DeviceDTOConverter deviceDTOConverter;
    private final DeviceTypeDTOConverter deviceTypeDTOConverter;

    @GetMapping("/list")
    public String getAllDevices(Model model) {
        List<Device> devices = deviceService.getAllDevices();
        List<DeviceDTO> deviceDTOs = new ArrayList<>();
        devices.forEach(device -> deviceDTOs.add(deviceDTOConverter.convertToDTO(device)));
        model.addAttribute("devices", deviceDTOs);
        return "devices/device_list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        List<DeviceType> deviceTypes = deviceTypeService.getAllDeviceTypes();
        List<DeviceTypeDTO> deviceTypeDTOs = new ArrayList<>();
        deviceTypes.forEach(deviceType -> deviceTypeDTOs.add(deviceTypeDTOConverter.convertToDTO(deviceType)));
        model.addAttribute("deviceTypes", deviceTypeDTOs);
        model.addAttribute("device", new DeviceDTO());
        return "devices/device_detail";
    }

    @PostMapping("/add")
    public String addDevice(@ModelAttribute("device") DeviceDTO deviceDTO) {
        Device device = deviceDTOConverter.convertToEntity(deviceDTO);
        deviceService.createDevice(device);
        return "redirect:/devices/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long deviceId, Model model) {
        Device device = deviceService.getDeviceById(deviceId);
        if (device != null) {
            List<DeviceType> deviceTypes = deviceTypeService.getAllDeviceTypes();
            List<DeviceTypeDTO> deviceTypeDTOs = new ArrayList<>();
            deviceTypes.forEach(deviceType -> deviceTypeDTOs.add(deviceTypeDTOConverter.convertToDTO(deviceType)));
            model.addAttribute("deviceTypes", deviceTypeDTOs);
            model.addAttribute("device", deviceDTOConverter.convertToDTO(device));
            return "devices/device_detail";
        } else {
            return "redirect:/devices/list";
        }
    }

    @PostMapping("/edit/{id}")
    public String editDevice(@PathVariable("id") Long deviceId, @ModelAttribute("device") DeviceDTO deviceDTO) {
        Device device = deviceDTOConverter.convertToEntity(deviceDTO);
        deviceService.updateDevice(deviceId, device);
        return "redirect:/devices/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteDevice(@PathVariable("id") Long deviceId) {
        deviceService.deleteDevice(deviceId);
        return "redirect:/devices/list";
    }
}
