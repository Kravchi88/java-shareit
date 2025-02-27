package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record BookingDto(
        Long id,

        @NotNull
        LocalDateTime start,

        @NotNull
        LocalDateTime end,

        @NotNull
        Long itemId
) {}