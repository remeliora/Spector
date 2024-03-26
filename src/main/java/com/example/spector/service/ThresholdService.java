package com.example.spector.service;

import com.example.spector.domain.Threshold;
import com.example.spector.repositories.ThresholdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ThresholdService {
    @Autowired
    private ThresholdRepository thresholdRepository;

    public Threshold createThreshold(Threshold threshold) {
        return thresholdRepository.save(threshold);
    }

    public Iterable<Threshold> getAllThresholds() {
        return thresholdRepository.findAll();
    }

    public Threshold getThresholdById(Long thresholdId) {
        return thresholdRepository.findById(thresholdId).orElse(null);
    }

    public Threshold updateThreshold(Long thresholdId, Threshold threshold) {
        Threshold updatedThreshold = thresholdRepository.findById(thresholdId).orElse(null);
        if (updatedThreshold != null) {
            updatedThreshold.setHighValue(threshold.getHighValue());
            updatedThreshold.setLowValue(threshold.getLowValue());
            updatedThreshold.setIsEnable(threshold.getIsEnable());

            return thresholdRepository.save(updatedThreshold);
        } else {
            return null;
        }
    }

    public void deleteThreshold(Long thresholdId) {
        thresholdRepository.deleteById(thresholdId);
    }
}
