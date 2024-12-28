package com.example.spector.checker.threshold;

import com.example.spector.domain.dto.DeviceDTO;
import com.example.spector.domain.dto.ThresholdDTO;

import java.util.List;

public interface ThresholdChecker {
    void checkThresholds(Object value, List<ThresholdDTO> thresholdDTOList, DeviceDTO deviceDTO);
}
