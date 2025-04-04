package com.example.spector.modules.datapattern;

import com.example.spector.domain.ParameterData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BaseSNMPStatus {
    public String determineStatus (List<ParameterData> parameterDataList) {
        // Если хотя бы один параметр имеет статус NO_DATA, то статус устройства ERROR
        if (parameterDataList.stream().anyMatch(pd -> "NO_DATA".equals(pd.getStatus()))) {
            return "ERROR";
        }
        // Если хотя бы один параметр имеет статус WARNING, то статус устройства WARNING
        if (parameterDataList.stream().anyMatch(pd -> "ERROR".equals(pd.getStatus()))) {
            return "ERROR";
        }
        // Если все параметры в порядке, статус устройства OK
        return "OK";
    }
}
