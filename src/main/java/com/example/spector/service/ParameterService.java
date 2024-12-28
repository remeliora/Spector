package com.example.spector.service;

import com.example.spector.domain.Parameter;
import com.example.spector.repositories.ParameterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParameterService {
    private final ParameterRepository parameterRepository;

    public Parameter createParameter(Parameter parameter) {
        return parameterRepository.save(parameter);
    }

    public List<Parameter> getAllParameters() {
        return parameterRepository.findAll();
    }

    public Parameter getParameterById(Long parameterId) {
        return parameterRepository.findById(parameterId).orElse(null);
    }

    public Parameter updateParameter(Long parameterId, Parameter parameter) {
        Parameter updatedParameter = parameterRepository.findById(parameterId).orElse(null);
        if (updatedParameter != null) {
            updatedParameter.setName(parameter.getName());
            updatedParameter.setAddress(parameter.getAddress());
            updatedParameter.setDataType(parameter.getDataType());
            updatedParameter.setMetric(parameter.getMetric());
            updatedParameter.setAdditive(parameter.getAdditive());
            updatedParameter.setCoefficient(parameter.getCoefficient());
            updatedParameter.setDescription(parameter.getDescription());

            return parameterRepository.save(updatedParameter);
        } else {
            return null;
        }
    }

    public void deleteParameter(Long parameterId) {
        parameterRepository.deleteById(parameterId);
    }
}
