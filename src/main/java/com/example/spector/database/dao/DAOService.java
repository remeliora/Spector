package com.example.spector.database.dao;

import com.example.spector.domain.dto.device.DeviceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class DAOService {
    @Qualifier("jsonDAO")
    private final DAO jsonDAO;

    @Qualifier("mongoDBDAO")
    private final DAO mongoDBDAO;

    public void prepareDAO(DeviceDTO deviceDTO) {
        jsonDAO.prepareDAO(deviceDTO);
        mongoDBDAO.prepareDAO(deviceDTO);
    }

    public void writeData(DeviceDTO deviceDTO, Map<String, Object> snmpData) {
        jsonDAO.writeData(deviceDTO, snmpData);
        mongoDBDAO.writeData(deviceDTO, snmpData);
    }
}
