package com.example.spector.controller.thymeleaf;

import com.example.spector.domain.DeviceType;
import com.example.spector.domain.dto.DeviceTypeDTO;
import com.example.spector.mapper.DeviceTypeDTOConverter;
import com.example.spector.service.DeviceTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/device-types")
public class DeviceTypeController {
    private final DeviceTypeService deviceTypeService;
    private final DeviceTypeDTOConverter deviceTypeDTOConverter;

    @GetMapping("/list")
    public String getAllDeviceTypes(Model model) {
        List<DeviceType> deviceTypes = deviceTypeService.getAllDeviceTypes();
        List<DeviceTypeDTO> deviceTypeDTOs = new ArrayList<>();
        deviceTypes.forEach(deviceType -> deviceTypeDTOs.add(deviceTypeDTOConverter.convertToDTO(deviceType)));
        model.addAttribute("deviceTypes", deviceTypeDTOs);
        return "devicetypes/devicetype_list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("deviceType", new DeviceTypeDTO());
        return "devicetypes/devicetype_detail";
    }

    @PostMapping("/add")
    public String addDeviceType(@ModelAttribute("deviceType") DeviceTypeDTO deviceTypeDTO) {
        DeviceType deviceType = deviceTypeDTOConverter.convertToEntity(deviceTypeDTO);
        deviceTypeService.createDeviceType(deviceType);
        return "redirect:/device-types/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long deviceTypeId, Model model) {
        DeviceType deviceType = deviceTypeService.getDeviceTypeById(deviceTypeId);
        if (deviceType != null) {
            model.addAttribute("deviceType", deviceTypeDTOConverter.convertToDTO(deviceType));
            return "devicetypes/devicetype_detail";
        } else {
            return "redirect:/device-types/list";
        }
    }

    @PostMapping("/edit/{id}")
    public String editDeviceType(@PathVariable("id") Long deviceTypeId, @ModelAttribute("deviceType") DeviceTypeDTO deviceTypeDTO) {
        DeviceType deviceType = deviceTypeDTOConverter.convertToEntity(deviceTypeDTO);
        deviceTypeService.updateDeviceType(deviceTypeId, deviceType);
        return "redirect:/device-types/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteDeviceType(@PathVariable("id") Long deviceTypeId) {
        deviceTypeService.deleteDeviceType(deviceTypeId);
        return "redirect:/device-types/list";
    }
}
