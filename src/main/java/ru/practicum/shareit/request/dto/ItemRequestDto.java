package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;

public record ItemRequestDto(@NotBlank String description) {
}