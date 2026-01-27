package com.example.spector.repositories;

import com.example.spector.domain.devicedata.DeviceData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceDataRepository extends MongoRepository<DeviceData, String> {
}
