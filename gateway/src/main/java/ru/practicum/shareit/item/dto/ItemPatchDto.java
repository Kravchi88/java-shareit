package ru.practicum.shareit.item.dto;

public record ItemPatchDto(
        Long id,
        String name,
        String description,
        Boolean available,
        Long ownerId,
        Long requestId
) {}
