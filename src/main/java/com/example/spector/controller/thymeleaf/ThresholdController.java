package com.example.spector.controller.thymeleaf;

import com.example.spector.domain.Device;
import com.example.spector.domain.Parameter;
import com.example.spector.domain.Threshold;
import com.example.spector.domain.dto.DeviceDTO;
import com.example.spector.domain.dto.ParameterDTO;
import com.example.spector.domain.dto.ThresholdDTO;
import com.example.spector.mapper.DeviceDTOConverter;
import com.example.spector.mapper.ParameterDTOConverter;
import com.example.spector.mapper.ThresholdDTOConverter;
import com.example.spector.service.DeviceService;
import com.example.spector.service.ParameterService;
import com.example.spector.service.ThresholdService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/thresholds")
public class ThresholdController {
    private final ThresholdService thresholdService;
    private final ParameterService parameterService;
    private final DeviceService deviceService;
    private final ThresholdDTOConverter thresholdDTOConverter;
    private final ParameterDTOConverter parameterDTOConverter;
    private final DeviceDTOConverter deviceDTOConverter;

    @GetMapping("/list")
    public String getAllThresholds(Model model) {
        Iterable<Threshold> thresholds = thresholdService.getAllThresholds();
        List<ThresholdDTO> thresholdDTOs = new ArrayList<>();
        thresholds.forEach(threshold -> thresholdDTOs.add(thresholdDTOConverter.convertToDTO(threshold)));
        model.addAttribute("thresholds", thresholdDTOs);
        return "thresholds/threshold_list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        Iterable<Parameter> parameters = parameterService.getAllParameters();
        Iterable<Device> devices = deviceService.getAllDevices();
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
            Iterable<Parameter> parameters = parameterService.getAllParameters();
            Iterable<Device> devices = deviceService.getAllDevices();
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
