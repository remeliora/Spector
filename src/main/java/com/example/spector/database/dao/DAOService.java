package com.example.spector.database.dao;

import com.example.spector.domain.device.Device;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class DAOService {
//    @Qualifier("jsonDAO")
//    private final DAO jsonDAO;

    @Qualifier("mongoDBDAO")
    private final DAO mongoDBDAO;

    public void prepareDAO(Device device) {
//        jsonDAO.prepareDAO(deviceDTO);
        mongoDBDAO.prepareDAO(device);
    }

    public void writeData(Device device, Map<String, Object> snmpData) {
//        jsonDAO.writeData(deviceDTO, snmpData);
        mongoDBDAO.writeData(device, snmpData);
    }
}
