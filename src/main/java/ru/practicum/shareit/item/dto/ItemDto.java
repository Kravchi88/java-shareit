package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ItemDto(
        Long id,
        @NotBlank
        String name,
        @NotBlank
        String description,
        @NotNull
        Boolean available,
        Long ownerId,
        Long requestId
) {}
