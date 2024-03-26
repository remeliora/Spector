package com.example.spector.controller.rest;

import com.example.spector.domain.Threshold;
import com.example.spector.service.ThresholdService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${application.endpoint.root}")
@RequiredArgsConstructor
public class ThresholdRestController {
    @Autowired
    private ThresholdService thresholdService;

    @PostMapping("${application.endpoint.threshold}")
    public ResponseEntity<Threshold> createThreshold(@RequestBody Threshold threshold) {
        Threshold createdthreshold = thresholdService.createThreshold(threshold);
        return new ResponseEntity<>(createdthreshold, HttpStatus.CREATED);
    }

    @GetMapping("${application.endpoint.threshold}")
    public ResponseEntity<Iterable<Threshold>> getAllThresholds() {
        Iterable<Threshold> thresholds = thresholdService.getAllThresholds();
        return new ResponseEntity<>(thresholds, HttpStatus.OK);
    }

    @GetMapping("${application.endpoint.threshold}/{thresholdId}")
    public ResponseEntity<Threshold> getThresholdById(@PathVariable Long thresholdId) {
        Threshold threshold = thresholdService.getThresholdById(thresholdId);
        if (threshold != null) {
            return new ResponseEntity<>(threshold, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("${application.endpoint.threshold}/{thresholdId}")
    public ResponseEntity<Threshold> updateDevice(@PathVariable Long thresholdId, @RequestBody Threshold threshold) {
        Threshold updateThreshold = thresholdService.updateThreshold(thresholdId, threshold);
        if (updateThreshold != null) {
            return new ResponseEntity<>(updateThreshold, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("${application.endpoint.threshold}/{thresholdId}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long thresholdId) {
        thresholdService.deleteThreshold(thresholdId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
