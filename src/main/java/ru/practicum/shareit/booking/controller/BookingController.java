package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private static final String REQUEST_HEADER = "X-Sharer-User-Id";

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponseDto createBooking(
            @RequestHeader(REQUEST_HEADER) final long userId,
            @Valid @RequestBody() final BookingDto bookingDto) {
        return bookingService.createBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.OK)
    public BookingResponseDto approveBooking(
            @RequestHeader(REQUEST_HEADER) final long userId,
            @PathVariable("bookingId") final long bookingId,
            @RequestParam("approved") final boolean approved) {
        return bookingService.approveBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBooking(
            @RequestHeader(REQUEST_HEADER) final long userId,
            @PathVariable("bookingId") final long bookingId) {
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingResponseDto> getUserBookings(
            @RequestHeader(REQUEST_HEADER) final long userId,
            @RequestParam(value = "state", defaultValue = "ALL") final String state) {

        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown state: " + state);
        }

        return bookingService.getUserBookings(userId, bookingState);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getOwnerBookings(
            @RequestHeader(REQUEST_HEADER) final long userId,
            @RequestParam(value = "state", defaultValue = "ALL") final String state) {

        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown state: " + state);
        }

        return bookingService.getOwnerBookings(userId, bookingState);
    }
}
