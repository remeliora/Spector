package com.example.spector.repositories;

import com.example.spector.domain.AppSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppSettingRepository extends JpaRepository<AppSetting, Long> {
    Optional<AppSetting> findFirstBy();
}
