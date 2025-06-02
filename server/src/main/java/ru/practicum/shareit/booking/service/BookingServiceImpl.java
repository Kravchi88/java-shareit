package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingResponseDto createBooking(Long userId, BookingDto bookingDto) {
        User user = checkUserExists(userId);
        Item item = getBookingItem(bookingDto.itemId());

        Booking booking = BookingMapper.toBooking(bookingDto, item, user);

        if (userId.equals(item.getOwner().getId())) {
            throw new ForbiddenException("User can't book their items");
        }

        if (!item.getAvailable()) {
            throw new ValidationException("Item " + item.getName() + " is unavailable now");
        }

        if (!booking.getEnd().isAfter(booking.getStart())) {
            throw new ValidationException("Invalid booking dates");
        }

        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(Long userId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));

        if (!userId.equals(booking.getItem().getOwner().getId())) {
            throw new ForbiddenException("Access denied. Only owner can approve bookings");
        }

        if (booking.getStatus().equals(Booking.BookingStatus.WAITING)) {
            if (approved) {
                booking.setStatus(Booking.BookingStatus.APPROVED);
            } else {
                booking.setStatus(Booking.BookingStatus.REJECTED);
            }
        } else {
            throw new ForbiddenException("Status already defined");
        }

        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto getBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));

        if (!userId.equals(booking.getBooker().getId()) && !userId.equals(booking.getItem().getOwner().getId())) {
            throw new ForbiddenException("Access denied. Only booker and owner allowed");
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, BookingState bookingState, int from, int size) {
        checkUserExists(userId);
        LocalDateTime now = LocalDateTime.now();

        Pageable pageable = PageRequest.of(from / size, size,
                Sort.by(Sort.Direction.DESC, "start"));

        List<Booking> bookings = switch (bookingState) {
            case CURRENT -> bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now, pageable);
            case PAST -> bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, now, pageable);
            case FUTURE -> bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now, pageable);
            case WAITING ->
                    bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Booking.BookingStatus.WAITING, pageable);
            case REJECTED ->
                    bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Booking.BookingStatus.REJECTED, pageable);
            default -> bookingRepository.findByBookerIdOrderByStartDesc(userId, pageable);
        };

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long userId, BookingState bookingState, int from, int size) {
        checkUserExists(userId);
        LocalDateTime now = LocalDateTime.now();

        Pageable pageable = PageRequest.of(from / size, size,
                Sort.by(Sort.Direction.DESC, "start"));

        List<Booking> bookings = switch (bookingState) {
            case CURRENT ->
                    bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now, pageable);
            case PAST -> bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now, pageable);
            case FUTURE -> bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now, pageable);
            case WAITING ->
                    bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, Booking.BookingStatus.WAITING, pageable);
            case REJECTED ->
                    bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, Booking.BookingStatus.REJECTED, pageable);
            default -> bookingRepository.findByItemOwnerIdOrderByStartDesc(userId, pageable);
        };

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    private User checkUserExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private Item getBookingItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));
    }
}
