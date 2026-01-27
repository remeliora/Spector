package com.example.spector.service;

import com.example.spector.domain.parameter.Parameter;
import com.example.spector.domain.statusdictionary.StatusDictionary;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.domain.statusdictionary.dto.StatusDictionaryBaseDTO;
import com.example.spector.domain.statusdictionary.dto.StatusDictionaryCreateDTO;
import com.example.spector.domain.statusdictionary.dto.StatusDictionaryDetailDTO;
import com.example.spector.domain.statusdictionary.dto.StatusDictionaryUpdateDTO;
import com.example.spector.mapper.BaseDTOConverter;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.modules.event.EventMessage;
import com.example.spector.repositories.ParameterRepository;
import com.example.spector.repositories.StatusDictionaryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
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
    public StatusDictionaryDetailDTO createStatusDictionary(StatusDictionaryCreateDTO createDTO,
                                                            String clientIp, EventDispatcher eventDispatcher) {
        String name = createDTO.getName();

        // Проверяем, существует ли словарь с таким именем
        if (statusDictionaryRepository.findStatusDictionaryByName(name).isPresent()) {
            throw new IllegalArgumentException("Status Dictionary already exists with name: " + name);
        }

        // Создаём сущность из DTO
        StatusDictionary newDictionary = baseDTOConverter.toEntity(createDTO, StatusDictionary.class);

        StatusDictionary savedDictionary = statusDictionaryRepository.save(newDictionary);

        String message = String.format("IP %s: User created status dictionary: name='%s', values count=%d",
                clientIp, savedDictionary.getName(), savedDictionary.getEnumValues().size());
        EventMessage event = EventMessage.log(EventType.REQUEST, MessageType.INFO, message);
        eventDispatcher.dispatch(event);

        // Преобразуем сохранённый объект в DTO для возврата
        return baseDTOConverter.toDTO(savedDictionary, StatusDictionaryDetailDTO.class);
    }

    // Обновление словаря
    @Transactional
    public StatusDictionaryDetailDTO updateStatusDictionary(Long id, StatusDictionaryUpdateDTO updateDTO,
                                                            String clientIp, EventDispatcher eventDispatcher) {
        // Находим существующий словарь по ID
        StatusDictionary existingDictionary = statusDictionaryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Status Dictionary not found with id: " + id));

        String oldName = existingDictionary.getName();
        int oldCount = existingDictionary.getEnumValues().size();

        // Обновляем поля
        existingDictionary.setName(updateDTO.getName());
        existingDictionary.setEnumValues(updateDTO.getEnumValues());

        StatusDictionary updatedDictionary = statusDictionaryRepository.save(existingDictionary);

        String changes = "";
        if (!Objects.equals(oldName, updateDTO.getName())) {
            changes += String.format("name: '%s' -> '%s', ", oldName, updateDTO.getName());
        }
        if (oldCount != updateDTO.getEnumValues().size()) {
            changes += String.format("values count: %d -> %d, ", oldCount, updateDTO.getEnumValues().size());
        }

        if (!changes.isEmpty()) {
            changes = changes.substring(0, changes.length() - 2); // Убираем последнюю запятую
            String message = String.format("IP %s: User updated status dictionary ID %d: %s",
                    clientIp, id, changes);
            EventMessage event = EventMessage.log(EventType.REQUEST, MessageType.INFO, message);
            eventDispatcher.dispatch(event);
        }

        // Преобразуем обновлённый объект в DTO для возврата
        return baseDTOConverter.toDTO(updatedDictionary, StatusDictionaryDetailDTO.class);
    }

    // Удаление словаря
    @Transactional
    public void deleteStatusDictionary(Long id, String clientIp, EventDispatcher eventDispatcher) {
        StatusDictionary dictionary = statusDictionaryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Status Dictionary not found with id: " + id));

        String name = dictionary.getName();
        int valueCount = dictionary.getEnumValues().size();

        // Находим параметры, использующие этот словарь
        List<Parameter> parametersUsingDict = parameterRepository.findByStatusDictionaryId(dictionary.getId());

        // Обнуляем ссылку на словарь в этих параметрах и сохраняем их
        for (Parameter param : parametersUsingDict) {
            param.setStatusDictionary(null); // Сбрасываем связь
            parameterRepository.save(param); // Явно сохраняем параметр с обновлённой связью
        }

        // Удаляем сам словарь
        statusDictionaryRepository.delete(dictionary);

        String message = String.format("IP %s: User deleted status dictionary ID %d: name='%s', values count=%d",
                clientIp, id, name, valueCount);
        EventMessage event = EventMessage.log(EventType.REQUEST, MessageType.INFO, message);
        eventDispatcher.dispatch(event);
    }
}
