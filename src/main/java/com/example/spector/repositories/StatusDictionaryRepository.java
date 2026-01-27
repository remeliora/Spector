package com.example.spector.repositories;

import com.example.spector.domain.statusdictionary.StatusDictionary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatusDictionaryRepository extends JpaRepository<StatusDictionary, Long> {
    Optional<StatusDictionary> findStatusDictionaryByName(String name);
}