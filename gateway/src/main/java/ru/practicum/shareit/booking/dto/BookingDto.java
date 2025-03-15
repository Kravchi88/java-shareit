package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record BookingDto(
        Long id,
        @NotNull
        @FutureOrPresent
        LocalDateTime start,
        @NotNull
        @Future
        LocalDateTime end,
        @NotNull
        Long itemId
) {}