package com.example.spector.controller.thymeleaf;

import com.example.spector.domain.Device;
import com.example.spector.domain.DeviceType;
import com.example.spector.service.DeviceService;
import com.example.spector.service.DeviceTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/devices")
public class DeviceController {
    @Autowired
    private DeviceService deviceService;

    @Autowired
    private DeviceTypeService deviceTypeService;

    @GetMapping("/list")
    public String getAllDevices(Model model) {
        Iterable<Device> devices = deviceService.getAllDevices();
        model.addAttribute("devices", devices);
        return "devices/device_list";
    }
    @GetMapping("/add")
    public String showAddForm(Model model) {
        Iterable<DeviceType> deviceTypes = deviceTypeService.getAllDeviceTypes();
        model.addAttribute("deviceTypes", deviceTypes);
        model.addAttribute("device", new Device());
        return "devices/device_detail";
    }

    @PostMapping("/add")
    public String addDevice(@ModelAttribute("device") Device device) {
        deviceService.createDevice(device);
        return "redirect:/devices/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long deviceId, Model model) {
        Device device = deviceService.getDeviceById(deviceId);
        if (device != null) {
            Iterable<DeviceType> deviceTypes = deviceTypeService.getAllDeviceTypes();
            model.addAttribute("deviceTypes", deviceTypes);
            model.addAttribute("device", device);
            return "devices/device_detail";
        } else {
            return "redirect:/devices/list";
        }
    }

    @PostMapping("/edit/{id}")
    public String editDevice(@PathVariable("id") Long deviceId, @ModelAttribute("device") Device device) {
        deviceService.updateDevice(deviceId, device);
        return "redirect:/devices/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteDevice(@PathVariable("id") Long deviceId) {
        deviceService.deleteDevice(deviceId);
        return "redirect:/devices/list";
    }
}
