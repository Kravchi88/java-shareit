package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User user;
    private User owner;
    private Item item;
    private Booking booking;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "John Doe", "john.doe@example.com");
        owner = new User(2L, "Jane Doe", "jane.doe@example.com");
        item = new Item(1L, "Drill", "Powerful drill", true, owner, null);
        booking = new Booking(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, user, Booking.BookingStatus.WAITING);
        bookingDto = new BookingDto(null, booking.getStart(), booking.getEnd(), item.getId());
    }

    @Test
    void createBooking_Success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponseDto result = bookingService.createBooking(user.getId(), bookingDto);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(Booking.BookingStatus.WAITING);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_UserIsOwner_ShouldThrowForbiddenException() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(owner.getId(), bookingDto))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("User can't book their items");
    }

    @Test
    void createBooking_ItemUnavailable_ShouldThrowValidationException() {
        item.setAvailable(false);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(user.getId(), bookingDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Item Drill is unavailable now");
    }

    @Test
    void createBooking_InvalidDates_ShouldThrowValidationException() {
        bookingDto = new BookingDto(null, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(1), item.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(user.getId(), bookingDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid booking dates");
    }

    @Test
    void approveBooking_Success() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponseDto result = bookingService.approveBooking(owner.getId(), booking.getId(), true);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(Booking.BookingStatus.APPROVED);
        verify(bookingRepository).save(booking);
    }

    @Test
    void approveBooking_UserNotOwner_ShouldThrowForbiddenException() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approveBooking(user.getId(), booking.getId(), true))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Only owner can approve bookings");
    }

    @Test
    void approveBooking_AlreadyApproved_ShouldThrowForbiddenException() {
        booking.setStatus(Booking.BookingStatus.APPROVED);
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approveBooking(owner.getId(), booking.getId(), true))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Status already defined");
    }

    @Test
    void getBookingById_Success() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.getBookingById(user.getId(), booking.getId());

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(Booking.BookingStatus.WAITING);
    }

    @Test
    void getBookingById_NotBookerOrOwner_ShouldThrowForbiddenException() {
        User anotherUser = new User(3L, "Random User", "random@example.com");
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.getBookingById(anotherUser.getId(), booking.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    void getUserBookings_Success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdOrderByStartDesc(eq(user.getId()), any())).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getUserBookings(user.getId(), BookingState.ALL, 0, 10);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).status()).isEqualTo(Booking.BookingStatus.WAITING);
    }

    @Test
    void getOwnerBookings_Success() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(eq(owner.getId()), any())).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(owner.getId(), BookingState.ALL, 0, 10);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).status()).isEqualTo(Booking.BookingStatus.WAITING);
    }

    @Test
    void createBooking_UserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(user.getId(), bookingDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createBooking_ItemNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(user.getId(), bookingDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Item not found");
    }

    @Test
    void approveBooking_BookingNotFound_ShouldThrowNotFoundException() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.approveBooking(owner.getId(), booking.getId(), true))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Booking not found");
    }

    @Test
    void getBookingById_BookingNotFound_ShouldThrowNotFoundException() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingById(user.getId(), booking.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Booking not found");
    }

    @Test
    void getUserBookings_Current() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(eq(user.getId()), any(), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getUserBookings(user.getId(), BookingState.CURRENT, 0, 10);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).status()).isEqualTo(Booking.BookingStatus.WAITING);
    }

    @Test
    void getUserBookings_Past() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(eq(user.getId()), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getUserBookings(user.getId(), BookingState.PAST, 0, 10);

        assertThat(result).isNotEmpty();
    }

    @Test
    void getUserBookings_Future() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(eq(user.getId()), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getUserBookings(user.getId(), BookingState.FUTURE, 0, 10);

        assertThat(result).isNotEmpty();
    }

    @Test
    void getUserBookings_Waiting() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(eq(user.getId()), eq(Booking.BookingStatus.WAITING), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getUserBookings(user.getId(), BookingState.WAITING, 0, 10);

        assertThat(result).isNotEmpty();
    }

    @Test
    void getUserBookings_Rejected() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(eq(user.getId()), eq(Booking.BookingStatus.REJECTED), any()))
                .thenReturn(List.of());

        List<BookingResponseDto> result = bookingService.getUserBookings(user.getId(), BookingState.REJECTED, 0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void getOwnerBookings_Current() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(eq(owner.getId()), any(), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(owner.getId(), BookingState.CURRENT, 0, 10);

        assertThat(result).isNotEmpty();
    }

    @Test
    void getOwnerBookings_Past() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(eq(owner.getId()), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(owner.getId(), BookingState.PAST, 0, 10);

        assertThat(result).isNotEmpty();
    }

    @Test
    void getOwnerBookings_Future() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(eq(owner.getId()), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(owner.getId(), BookingState.FUTURE, 0, 10);

        assertThat(result).isNotEmpty();
    }

    @Test
    void getOwnerBookings_Waiting() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(eq(owner.getId()), eq(Booking.BookingStatus.WAITING), any()))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(owner.getId(), BookingState.WAITING, 0, 10);

        assertThat(result).isNotEmpty();
    }

    @Test
    void getOwnerBookings_Rejected() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(eq(owner.getId()), eq(Booking.BookingStatus.REJECTED), any()))
                .thenReturn(List.of());

        List<BookingResponseDto> result = bookingService.getOwnerBookings(owner.getId(), BookingState.REJECTED, 0, 10);

        assertThat(result).isEmpty();
    }
}