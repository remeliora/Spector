package com.example.spector.controller.thymeleaf;

import com.example.spector.domain.DeviceType;
import com.example.spector.domain.Parameter;
import com.example.spector.service.DeviceTypeService;
import com.example.spector.service.ParameterService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/parameters")
public class ParameterController {
    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DeviceTypeService deviceTypeService;

    @GetMapping("/list")
    public String getAllParameters(Model model) {
        Iterable<Parameter> parameters = parameterService.getAllParameters();
        model.addAttribute("parameters", parameters);
        return "parameters/parameter_list";
    }
    @GetMapping("/add")
    public String showAddForm(Model model) {
        Iterable<DeviceType> deviceTypes = deviceTypeService.getAllDeviceTypes();
        model.addAttribute("deviceTypes", deviceTypes);
        model.addAttribute("parameter", new Parameter());
        return "parameters/parameter_detail";
    }

    @PostMapping("/add")
    public String addParameter(@ModelAttribute("parameter") Parameter parameter) {
        parameterService.createParameter(parameter);
        return "redirect:/parameters/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long parameterId, Model model) {
        Parameter parameter = parameterService.getParameterById(parameterId);
        if (parameter != null) {
            Iterable<DeviceType> deviceTypes = deviceTypeService.getAllDeviceTypes();
            model.addAttribute("deviceTypes", deviceTypes);
            model.addAttribute("parameter", parameter);
            return "parameters/parameter_detail";
        } else {
            return "redirect:/parameters/list";
        }
    }

    @PostMapping("/edit/{id}")
    public String editParameter(@PathVariable("id") Long parameterId, @ModelAttribute("parameter") Parameter parameter) {
        parameterService.updateParameter(parameterId, parameter);
        return "redirect:/parameters/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteParameter(@PathVariable("id") Long parameterId) {
        parameterService.deleteParameter(parameterId);
        return "redirect:/parameters/list";
    }
}
