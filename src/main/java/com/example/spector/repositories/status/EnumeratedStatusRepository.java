package com.example.spector.repositories.status;

import com.example.spector.domain.EnumeratedStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EnumeratedStatusRepository extends MongoRepository<EnumeratedStatus, String> {
}
