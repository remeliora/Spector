package com.example.spector.service;

import com.example.spector.domain.Threshold;
import com.example.spector.repositories.ThresholdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class ThresholdService {
    private final ThresholdRepository thresholdRepository;

    private static final Logger logger = Logger.getLogger(ThresholdService.class.getName());

    public Threshold createThreshold(Threshold threshold) {
        return thresholdRepository.save(threshold);
    }

    public List<Threshold> getAllThresholds() {
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
//        logger.log(Level.INFO, "Starting deleteThreshold method");

        Optional<Threshold> thresholdOptional = thresholdRepository.findById(thresholdId);
        if (thresholdOptional.isPresent()) {
            Threshold threshold = thresholdOptional.get();
            thresholdRepository.deleteById(thresholdId);
        }

//        logger.log(Level.INFO, "Threshold with ID {0} deleted", thresholdId);
    }
}
