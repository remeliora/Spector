package com.example.spector.controller.thymeleaf;

import com.example.spector.domain.Device;
import com.example.spector.domain.Parameter;
import com.example.spector.domain.Threshold;
import com.example.spector.service.DeviceService;
import com.example.spector.service.ParameterService;
import com.example.spector.service.ThresholdService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/thresholds")
public class ThresholdController {
    @Autowired
    private ThresholdService thresholdService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DeviceService deviceService;

    @GetMapping("/list")
    public String getAllThresholds(Model model) {
        Iterable<Threshold> thresholds = thresholdService.getAllThresholds();
        model.addAttribute("thresholds", thresholds);
        return "thresholds/threshold_list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        Iterable<Parameter> parameters = parameterService.getAllParameters();
        Iterable<Device> devices = deviceService.getAllDevices();
        model.addAttribute("parameters", parameters);
        model.addAttribute("devices", devices);
        model.addAttribute("threshold", new Threshold());
        return "thresholds/threshold_detail";
    }

    @PostMapping("/add")
    public String addThreshold(@ModelAttribute("threshold") Threshold threshold) {
        thresholdService.createThreshold(threshold);
        return "redirect:/thresholds/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long thresholdId, Model model) {
        Threshold threshold = thresholdService.getThresholdById(thresholdId);
        if (threshold != null) {
            Iterable<Parameter> parameters = parameterService.getAllParameters();
            Iterable<Device> devices = deviceService.getAllDevices();
            model.addAttribute("parameters", parameters);
            model.addAttribute("devices", devices);
            model.addAttribute("threshold", threshold);
            return "thresholds/threshold_detail";
        } else {
            return "redirect:/thresholds/list";
        }
    }

    @PostMapping("/edit/{id}")
    public String editThreshold(@PathVariable("id") Long thresholdId, @ModelAttribute("threshold") Threshold threshold) {
        thresholdService.updateThreshold(thresholdId, threshold);
        return "redirect:/thresholds/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteThreshold(@PathVariable("id") Long thresholdId) {
        thresholdService.deleteThreshold(thresholdId);
        return "redirect:/thresholds/list";
    }
}
