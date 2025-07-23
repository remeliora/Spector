package com.example.spector.mapper;

public interface ConverterDTO<E, D> {
    //  Конвертация сущности в DTO-класс
    D convertToDTO(E entity);

    //  Конвертация DTO-класса в сущность
    E convertToEntity(D dto);
}
