package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

public record BookingDto(
        Long id,
        LocalDateTime start,
        LocalDateTime end,
        Long itemId
) {}