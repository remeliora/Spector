package com.example.spector.service;

import com.example.spector.domain.Parameter;
import com.example.spector.domain.StatusDictionary;
import com.example.spector.domain.dto.statusdictionary.*;
import com.example.spector.mapper.BaseDTOConverter;
import com.example.spector.repositories.ParameterRepository;
import com.example.spector.repositories.StatusDictionaryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatusDictionaryService {
    private final StatusDictionaryRepository statusDictionaryRepository;
    private final ParameterRepository parameterRepository; // Для проверки использования
    private final BaseDTOConverter baseDTOConverter; // Для преобразования сущностей в DTO

    // Получение списка всех словарей
    public List<StatusDictionaryBaseDTO> getStatusDictionaries() {
        List<StatusDictionary> allDictionaries = statusDictionaryRepository.findAll();

        return allDictionaries.stream()
                .map(dict -> {
                    StatusDictionaryBaseDTO dto = new StatusDictionaryBaseDTO(); // Используем пустой конструктор
                    dto.setId(dict.getId());
                    dto.setName(dict.getName());
                    dto.setCount(dict.getEnumValues().size()); // Устанавливаем количество элементов
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Получение детальной информации о словаре по имени
    public StatusDictionaryDetailDTO getStatusDictionaryDetail(Long id) {
        StatusDictionary dictionary = statusDictionaryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Status Dictionary not found with id: " + id));

        // Преобразуем сущность в DTO
        return baseDTOConverter.toDTO(dictionary, StatusDictionaryDetailDTO.class);
    }

    //================
    //      CRUD
    //================

    // Создание нового словаря
    @Transactional
    public StatusDictionaryDetailDTO createStatusDictionary(StatusDictionaryCreateDTO createDTO) {
        String name = createDTO.getName();

        // Проверяем, существует ли словарь с таким именем
        if (statusDictionaryRepository.findStatusDictionaryByName(name).isPresent()) {
            throw new IllegalArgumentException("Status Dictionary already exists with name: " + name);
        }

        // Создаём сущность из DTO
        StatusDictionary newDictionary = baseDTOConverter.toEntity(createDTO, StatusDictionary.class);

        StatusDictionary savedDictionary = statusDictionaryRepository.save(newDictionary);

        // Преобразуем сохранённый объект в DTO для возврата
        return baseDTOConverter.toDTO(savedDictionary, StatusDictionaryDetailDTO.class);
    }

    // Обновление словаря
    @Transactional
    public StatusDictionaryDetailDTO updateStatusDictionary(Long id, StatusDictionaryUpdateDTO updateDTO) {
        // Находим существующий словарь по ID
        StatusDictionary existingDictionary = statusDictionaryRepository.findById(id) // <-- Ищем по ID
                .orElseThrow(() -> new NoSuchElementException("Status Dictionary not found with id: " + id));

        // Обновляем поля
        existingDictionary.setName(updateDTO.getName()); // Можно обновлять имя, если бизнес-логика позволяет
        existingDictionary.setEnumValues(updateDTO.getEnumValues());

        StatusDictionary updatedDictionary = statusDictionaryRepository.save(existingDictionary);

        // Преобразуем обновлённый объект в DTO для возврата
        return baseDTOConverter.toDTO(updatedDictionary, StatusDictionaryDetailDTO.class);
    }

    // Удаление словаря
    @Transactional
    public void deleteStatusDictionary(Long id) {
        StatusDictionary dictionary = statusDictionaryRepository.findById(id) // <-- Ищем по ID
                .orElseThrow(() -> new NoSuchElementException("Status Dictionary not found with id: " + id));

        // Находим параметры, использующие этот словарь
        List<Parameter> parametersUsingDict = parameterRepository.findByStatusDictionaryId(dictionary.getId());

        // Обнуляем ссылку на словарь в этих параметрах и сохраняем их
        for (Parameter param : parametersUsingDict) {
            param.setStatusDictionary(null); // Сбрасываем связь
            parameterRepository.save(param); // Явно сохраняем параметр с обновлённой связью
        }

        // Удаляем сам словарь
        statusDictionaryRepository.delete(dictionary);
    }

    // Получение списка StatusDictionary
    public List<StatusDictionaryShortDTO> getAllStatusDictionaries() {
        return statusDictionaryRepository.findAll()
                .stream()
                .map(sd -> {
                    StatusDictionaryShortDTO dto = new StatusDictionaryShortDTO();
                    dto.setId(sd.getId());
                    dto.setName(sd.getName());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
