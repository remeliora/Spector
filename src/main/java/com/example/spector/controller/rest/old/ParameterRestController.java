package com.example.spector.controller.rest.old;

import com.example.spector.domain.Parameter;
import com.example.spector.service.parameter.ParameterService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${application.endpoint.root}")
@RequiredArgsConstructor
public class ParameterRestController {
    @Autowired
    private ParameterService parameterService;

    @PostMapping("${application.endpoint.parameter}")
    public ResponseEntity<Parameter> createParameter(@RequestBody Parameter parameter) {
        Parameter createdParameter = parameterService.createParameter(parameter);
        return new ResponseEntity<>(createdParameter, HttpStatus.CREATED);
    }

    @GetMapping("${application.endpoint.parameter}")
    public ResponseEntity<Iterable<Parameter>> getAllParameters() {
        Iterable<Parameter> parameters = parameterService.getAllParameters();
        return new ResponseEntity<>(parameters, HttpStatus.OK);
    }

    @GetMapping("${application.endpoint.parameter}/{parameterId}")
    public ResponseEntity<Parameter> getParameterById(@PathVariable Long parameterId) {
        Parameter parameter = parameterService.getParameterById(parameterId);
        if (parameter != null) {
            return new ResponseEntity<>(parameter, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("${application.endpoint.parameter}/{parameterId}")
    public ResponseEntity<Parameter> updateParameter(@PathVariable Long parameterId, @RequestBody Parameter parameter) {
        Parameter updateParameter = parameterService.updateParameter(parameterId, parameter);
        if (updateParameter != null) {
            return new ResponseEntity<>(updateParameter, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("${application.endpoint.parameter}/{parameterId}")
    public ResponseEntity<Void> deleteParameter(@PathVariable Long parameterId) {
        parameterService.deleteParameter(parameterId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
