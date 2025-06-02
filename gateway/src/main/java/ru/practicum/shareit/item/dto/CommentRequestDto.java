package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentRequestDto(
        @NotBlank String text
) {}