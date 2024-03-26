package com.example.spector.controller.thymeleaf;

import com.example.spector.domain.DeviceType;
import com.example.spector.service.DeviceTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

//@Controller
//@RequiredArgsConstructor
//@RequestMapping("/device-types")
public class DeviceTypeController {

//    private final RestTemplate restTemplate;
//    private final String deviceTypeApiUrl = "http://localhost:8080/rest-api/v1/device-types";
//    private final DeviceTypeService deviceTypeService;
//    @GetMapping("/list")
//    public String showDeviceTypes(Model model) {
//        Iterable<DeviceType> deviceTypes = deviceTypeService.getAllDeviceTypes();
//        model.addAttribute("deviceTypes", deviceTypes);
//        return "device-types/list";
//    }
//
//    @GetMapping("/add")
//    public String showAddDeviceTypeForm(Model model) {
//        model.addAttribute("deviceType", new DeviceType());
//        return "device-types/add";
//    }
//
//    @GetMapping("/edit/{id}")
//    public String showEditDeviceTypeForm(@PathVariable("id") Long id, Model model) {
//        DeviceType deviceType = deviceTypeService.getDeviceTypeById(id);
//        model.addAttribute("deviceType", deviceType);
//        return "device-types/edit";
//    }
}
