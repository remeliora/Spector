package com.example.spector.controller.thymeleaf;

import com.example.spector.domain.DeviceType;
import com.example.spector.service.DeviceTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/device-types")
public class DeviceTypeController {
    @Autowired
    private DeviceTypeService deviceTypeService;

    @GetMapping("/list")
    public String getAllDeviceTypes(Model model) {
        Iterable<DeviceType> deviceTypes = deviceTypeService.getAllDeviceTypes();
        model.addAttribute("deviceTypes", deviceTypes);
        return "devicetypes/devicetype_list";
    }

    @GetMapping("/add")
    public  String showAddForm(Model model) {
        model.addAttribute("deviceType", new DeviceType());
        return "devicetypes/devicetype_detail";
    }

    @PostMapping("/add")
    public String addDeviceType(@ModelAttribute("deviceType") DeviceType deviceType) {
        deviceTypeService.createDeviceType(deviceType);
        return "redirect:/device-types/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long deviceTypeId, Model model) {
        DeviceType deviceType = deviceTypeService.getDeviceTypeById(deviceTypeId);
        if (deviceType != null) {
            model.addAttribute("deviceType", deviceType);
            return "devicetypes/devicetype_detail";
        } else {
            return "redirect:/device-types/list";
        }
    }

    @PostMapping("/edit/{id}")
    public String editDeviceType(@PathVariable("id") Long deviceTypeId, @ModelAttribute("deviceType") DeviceType deviceType) {
        deviceTypeService.updateDeviceType(deviceTypeId, deviceType);
        return "redirect:/device-types/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteDeviceType(@PathVariable("id") Long deviceTypeId) {
        deviceTypeService.deleteDeviceType(deviceTypeId);
        return "redirect:/device-types/list";
    }
}
