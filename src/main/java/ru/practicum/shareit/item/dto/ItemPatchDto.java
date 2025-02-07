package ru.practicum.shareit.item.dto;

public record ItemPatchDto(
        Long id,
        String name,
        String description,
        boolean available,
        long ownerId,
        Long requestId
) {}
