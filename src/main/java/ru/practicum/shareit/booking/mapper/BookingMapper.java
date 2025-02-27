package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserShortDto;
import ru.practicum.shareit.user.model.User;

public class BookingMapper {

    public static Booking toBooking(BookingDto dto, Item item, User booker) {
        return new Booking(
                dto.id(),
                dto.start(),
                dto.end(),
                item,
                booker,
                Booking.BookingStatus.WAITING
        );
    }

    public static BookingResponseDto toBookingDto(Booking booking) {
        return new BookingResponseDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                new UserShortDto(booking.getBooker().getId()),
                new ItemShortDto(booking.getItem().getId(), booking.getItem().getName())
        );
    }

    public static BookingShortDto toBookingShortDto(Booking booking) {
        return new BookingShortDto(
                booking.getId(),
                booking.getBooker().getId(),
                booking.getStart(),
                booking.getEnd()
        );
    }
}