package com.example.spector.controller.thymeleaf;

import com.example.spector.domain.Device;
import com.example.spector.domain.Parameter;
import com.example.spector.domain.Threshold;
import com.example.spector.domain.dto.device.DeviceDTO;
import com.example.spector.domain.dto.parameter.ParameterDTO;
import com.example.spector.domain.dto.threshold.ThresholdDTO;
import com.example.spector.mapper.DeviceDTOConverter;
import com.example.spector.mapper.ParameterDTOConverter;
import com.example.spector.mapper.ThresholdDTOConverter;
import com.example.spector.service.device.DeviceService;
import com.example.spector.service.parameter.ParameterService;
import com.example.spector.service.threshold.ThresholdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/thresholds")
public class ThresholdThymeleafController {
    private final ThresholdService thresholdService;
    private final ParameterService parameterService;
    private final DeviceService deviceService;
    private final ThresholdDTOConverter thresholdDTOConverter;
    private final ParameterDTOConverter parameterDTOConverter;
    private final DeviceDTOConverter deviceDTOConverter;

    @GetMapping("/list")
    public String getAllThresholds(Model model) {
        List<Threshold> thresholds = thresholdService.getAllThresholds();
        List<ThresholdDTO> thresholdDTOs = new ArrayList<>();
        thresholds.forEach(threshold -> thresholdDTOs.add(thresholdDTOConverter.convertToDTO(threshold)));
        model.addAttribute("thresholds", thresholdDTOs);
        return "thresholds/threshold_list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        List<Parameter> parameters = parameterService.getAllParameters();
        List<Device> devices = deviceService.getAllDevices();
        List<ParameterDTO> parameterDTOs = new ArrayList<>();
        List<DeviceDTO> deviceDTOs = new ArrayList<>();
        parameters.forEach(parameter -> parameterDTOs.add(parameterDTOConverter.convertToDTO(parameter)));
        devices.forEach(device -> deviceDTOs.add(deviceDTOConverter.convertToDTO(device)));
        model.addAttribute("parameters", parameterDTOs);
        model.addAttribute("devices", deviceDTOs);
        model.addAttribute("threshold", new ThresholdDTO());
        return "thresholds/threshold_detail";
    }

    @PostMapping("/add")
    public String addThreshold(@ModelAttribute("threshold") ThresholdDTO thresholdDTO) {
        Threshold threshold = thresholdDTOConverter.convertToEntity(thresholdDTO);
        thresholdService.createThreshold(threshold);
        return "redirect:/thresholds/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long thresholdId, Model model) {
        Threshold threshold = thresholdService.getThresholdById(thresholdId);
        if (threshold != null) {
            List<Parameter> parameters = parameterService.getAllParameters();
            List<Device> devices = deviceService.getAllDevices();
            List<ParameterDTO> parameterDTOs = new ArrayList<>();
            List<DeviceDTO> deviceDTOs = new ArrayList<>();
            parameters.forEach(parameter -> parameterDTOs.add(parameterDTOConverter.convertToDTO(parameter)));
            devices.forEach(device -> deviceDTOs.add(deviceDTOConverter.convertToDTO(device)));
            model.addAttribute("parameters", parameterDTOs);
            model.addAttribute("devices", deviceDTOs);
            model.addAttribute("threshold", thresholdDTOConverter.convertToDTO(threshold));
            return "thresholds/threshold_detail";
        } else {
            return "redirect:/thresholds/list";
        }
    }

    @PostMapping("/edit/{id}")
    public String editThreshold(@PathVariable("id") Long thresholdId, @ModelAttribute("threshold") ThresholdDTO thresholdDTO) {
        Threshold threshold = thresholdDTOConverter.convertToEntity(thresholdDTO);
        thresholdService.updateThreshold(thresholdId, threshold);
        return "redirect:/thresholds/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteThreshold(@PathVariable("id") Long thresholdId) {
        thresholdService.deleteThreshold(thresholdId);
        return "redirect:/thresholds/list";
    }
}
