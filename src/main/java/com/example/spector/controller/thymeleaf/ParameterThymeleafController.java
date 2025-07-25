package com.example.spector.controller.thymeleaf;

import com.example.spector.domain.DeviceType;
import com.example.spector.domain.Parameter;
import com.example.spector.domain.dto.devicetype.DeviceTypeDTO;
import com.example.spector.domain.dto.parameter.ParameterDTO;
import com.example.spector.mapper.DeviceTypeDTOConverter;
import com.example.spector.mapper.ParameterDTOConverter;
import com.example.spector.service.devicetype.DeviceTypeService;
import com.example.spector.service.parameter.ParameterService;
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
@RequestMapping("/parameters")
public class ParameterThymeleafController {
    private final ParameterService parameterService;
    private final DeviceTypeService deviceTypeService;
    private final ParameterDTOConverter parameterDTOConverter;
    private final DeviceTypeDTOConverter deviceTypeDTOConverter;

    @GetMapping("/list")
    public String getAllParameters(Model model) {
        List<Parameter> parameters = parameterService.getAllParameters();
        List<ParameterDTO> parameterDTOs = new ArrayList<>();
        parameters.forEach(parameter -> parameterDTOs.add(parameterDTOConverter.convertToDTO(parameter)));
        model.addAttribute("parameters", parameterDTOs);
        return "parameters/parameter_list";
    }
    @GetMapping("/add")
    public String showAddForm(Model model) {
        List<DeviceType> deviceTypes = deviceTypeService.getAllDeviceTypes();
        List<DeviceTypeDTO> deviceTypeDTOs = new ArrayList<>();
        deviceTypes.forEach(deviceType -> deviceTypeDTOs.add(deviceTypeDTOConverter.convertToDTO(deviceType)));
        model.addAttribute("deviceTypes", deviceTypeDTOs);
        model.addAttribute("parameter", new ParameterDTO());
        return "parameters/parameter_detail";
    }

    @PostMapping("/add")
    public String addParameter(@ModelAttribute("parameter") ParameterDTO parameterDTO) {
        Parameter parameter = parameterDTOConverter.convertToEntity(parameterDTO);
        parameterService.createParameter(parameter);
        return "redirect:/parameters/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long parameterId, Model model) {
        Parameter parameter = parameterService.getParameterById(parameterId);
        if (parameter != null) {
            List<DeviceType> deviceTypes = deviceTypeService.getAllDeviceTypes();
            List<DeviceTypeDTO> deviceTypeDTOs = new ArrayList<>();
            deviceTypes.forEach(deviceType -> deviceTypeDTOs.add(deviceTypeDTOConverter.convertToDTO(deviceType)));
            model.addAttribute("deviceTypes", deviceTypeDTOs);
            model.addAttribute("parameter", parameterDTOConverter.convertToDTO(parameter));
            return "parameters/parameter_detail";
        } else {
            return "redirect:/parameters/list";
        }
    }

    @PostMapping("/edit/{id}")
    public String editParameter(@PathVariable("id") Long parameterId, @ModelAttribute("parameter") ParameterDTO parameterDTO) {
        Parameter parameter = parameterDTOConverter.convertToEntity(parameterDTO);
        parameterService.updateParameter(parameterId, parameter);
        return "redirect:/parameters/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteParameter(@PathVariable("id") Long parameterId) {
        parameterService.deleteParameter(parameterId);
        return "redirect:/parameters/list";
    }
}
