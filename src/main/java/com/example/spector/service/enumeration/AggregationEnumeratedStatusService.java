package com.example.spector.service.enumeration;

import com.example.spector.domain.EnumeratedStatus;
import com.example.spector.domain.dto.enumeration.EnumeratedStatusBaseDTO;
import com.example.spector.domain.dto.enumeration.EnumeratedStatusCreateDTO;
import com.example.spector.domain.dto.enumeration.EnumeratedStatusDetailDTO;
import com.example.spector.mapper.BaseDTOConverter;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AggregationEnumeratedStatusService {
    private final BaseDTOConverter baseDTOConverter;
    private final MongoTemplate enumeratedStatusMongoTemplate;

    public AggregationEnumeratedStatusService(@Qualifier("databaseEnumeratedStatusMongoTemplate")
                                              MongoTemplate enumeratedStatusMongoTemplate,
                                              BaseDTOConverter baseDTOConverter) {
        this.enumeratedStatusMongoTemplate = enumeratedStatusMongoTemplate;
        this.baseDTOConverter = baseDTOConverter;
    }

    // Получение словаря
    public List<EnumeratedStatusBaseDTO> getEnumeratedStatuses() {
        List<String> collections = enumeratedStatusMongoTemplate.getCollectionNames()
                .stream()
                .filter(name -> !name.startsWith("system.")) // Игнорируем системные
                .toList();

        return collections.stream()
                .map(collection -> {
                    EnumeratedStatus status = enumeratedStatusMongoTemplate
                            .findAll(EnumeratedStatus.class, collection)
                            .get(0); // Берём первый документ

                    EnumeratedStatusBaseDTO dto = new EnumeratedStatusBaseDTO();
                    dto.setName(collection); // Имя коллекции = имя словаря
                    dto.setCount(status.getEnumValues().size());
                    return dto;
                })
                .toList();
    }

    // Получение детальной информации словаря
    public EnumeratedStatusDetailDTO getEnumeratedStatusesDetail(String collectionName) {
        EnumeratedStatus status = enumeratedStatusMongoTemplate.findOne(new Query(), EnumeratedStatus.class,
                collectionName);
        if (status == null) {
            throw new EntityNotFoundException("Enumerated Status not found with name: " + collectionName);
        }

        return baseDTOConverter.toDTO(status, EnumeratedStatusDetailDTO.class);
    }

    //================
    //      CRUD
    //================

    // Создание нового словаря
    @Transactional
    public EnumeratedStatusDetailDTO createEnumeratedStatus(EnumeratedStatusCreateDTO createDTO) {
        String collectionName = createDTO.getName();

        // Проверяем, существует ли коллекция с таким именем
        if (enumeratedStatusMongoTemplate.collectionExists(collectionName)) {
            System.out.println("Collection already exists: " + collectionName);
        }

        EnumeratedStatus newStatus = baseDTOConverter.toEntity(createDTO, EnumeratedStatus.class);
        EnumeratedStatus saveStatus = enumeratedStatusMongoTemplate.save(newStatus, collectionName);

        return baseDTOConverter.toDTO(saveStatus, EnumeratedStatusDetailDTO.class);
    }

    // Обновление словаря
    @Transactional
    public EnumeratedStatusDetailDTO updateEnumeratedStatus(String collectionName,
                                                            EnumeratedStatusDetailDTO updateDTO) {
        // Проверяем существование коллекции
        if (!enumeratedStatusMongoTemplate.collectionExists(collectionName)) {
            throw new EntityNotFoundException("Dictionary not found: " + collectionName);
        }

        // Получаем существующий документ (в коллекции всегда 1 документ)
        EnumeratedStatus existingStatus = enumeratedStatusMongoTemplate.findOne(
                new Query(), EnumeratedStatus.class, collectionName);

        if (existingStatus == null) {
            throw new EntityNotFoundException("Statuses not found in: " + collectionName);
        }

        // Обновляем только значения словаря (имя коллекции не меняем)
        existingStatus.setEnumValues(updateDTO.getEnumValues());

        EnumeratedStatus updateStatus = enumeratedStatusMongoTemplate.save(existingStatus, collectionName);

        return baseDTOConverter.toDTO(updateStatus, EnumeratedStatusDetailDTO.class);

    }

    @Transactional
    public void deleteEnumeratedStatus(String collectionName) {
        // Проверяем существование коллекции
        if (!enumeratedStatusMongoTemplate.collectionExists(collectionName)) {
            throw new EntityNotFoundException("Dictionary not found: " + collectionName);
        }

        // Удаляем всю коллекцию (со всеми документами)
        enumeratedStatusMongoTemplate.dropCollection(collectionName);
    }
}
