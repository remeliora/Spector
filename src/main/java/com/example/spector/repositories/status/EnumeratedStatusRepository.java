package com.example.spector.repositories.status;

import com.example.spector.domain.EnumeratedStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnumeratedStatusRepository extends MongoRepository<EnumeratedStatus, String> {
}
