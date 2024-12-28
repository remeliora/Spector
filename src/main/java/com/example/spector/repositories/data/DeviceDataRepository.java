package com.example.spector.repositories.data;

import com.example.spector.domain.DeviceData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface DeviceDataRepository extends MongoRepository<DeviceData, String> {
}
