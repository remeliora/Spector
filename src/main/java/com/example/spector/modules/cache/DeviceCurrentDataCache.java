package com.example.spector.modules.cache;

import com.example.spector.domain.DeviceCurrentData;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceCurrentDataCache {
    // Метод для обновления (или помещения) актуальных данных устройства в кэш
    @CachePut(value = "currentDeviceData", key = "#deviceId") // Кэшируем возвращаемое значение под ключом deviceId
    public DeviceCurrentData updateCurrentData(Long deviceId, DeviceCurrentData data) {
        return data; // Spring Cache положит это значение в кэш
    }

    // Метод для получения актуальных данных устройства из кэша
    @Cacheable(value = "currentDeviceData", key = "#deviceId", unless = "#result == null")
    // Кэшируем, если результат не null
    public DeviceCurrentData getCurrentData(Long deviceId) {
        // Этот метод вызывается Spring Cache при промахе.
        // В нашем случае, кэш обновляется через updateCurrentData,
        // и этот метод может не вызываться часто напрямую.
        // Если кэш пуст, можно вернуть null или заглушку.
        return null; // или возвращайте заглушку, если необходимо
    }

    // (Опционально) Метод для удаления данных из кэша (например, при выключении устройства)
    @CacheEvict(value = "currentDeviceData", key = "#deviceId")
    public void evictCurrentData(Long deviceId) {
    }
}
