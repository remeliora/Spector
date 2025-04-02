package com.example.spector.mapper;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BaseDTOConverter {
    private final ModelMapper modelMapper;

    public <E, D> D toDTO(E entity, Class<D> dtoClass) {
        return modelMapper.map(entity, dtoClass);
    }

    public <E, D> E toEntity(D dto, Class<E> entityClass) {
        return modelMapper.map(dto, entityClass);
    }
}
