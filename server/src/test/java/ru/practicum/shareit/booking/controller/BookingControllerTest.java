package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ApplicationExceptionHandler;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(BookingController.class)
@Import(ApplicationExceptionHandler.class)
class BookingControllerTest {

    private static final String REQUEST_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private final BookingResponseDto bookingResponse = new BookingResponseDto(
            1L,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2),
            Booking.BookingStatus.WAITING,
            new UserShortDto(1L),
            new ItemShortDto(1L, "Drill", 2L)
    );

    private final BookingDto bookingDto = new BookingDto(
            null,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2),
            1L
    );

    @Test
    void createBooking_Success() throws Exception {
        when(bookingService.createBooking(eq(1L), any(BookingDto.class))).thenReturn(bookingResponse);

        mockMvc.perform(post("/bookings")
                        .header(REQUEST_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(bookingResponse.id()));

        verify(bookingService, times(1)).createBooking(eq(1L), any(BookingDto.class));
    }

    @Test
    void approveBooking_Success() throws Exception {
        when(bookingService.approveBooking(1L, 1L, true)).thenReturn(bookingResponse);

        mockMvc.perform(patch("/bookings/{bookingId}", 1L)
                        .header(REQUEST_HEADER, "1")
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingResponse.id()));

        verify(bookingService, times(1)).approveBooking(1L, 1L, true);
    }

    @Test
    void getBooking_Success() throws Exception {
        when(bookingService.getBookingById(1L, 1L)).thenReturn(bookingResponse);

        mockMvc.perform(get("/bookings/{bookingId}", 1L)
                        .header(REQUEST_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingResponse.id()));

        verify(bookingService, times(1)).getBookingById(1L, 1L);
    }

    @Test
    void getBooking_NotFound() throws Exception {
        when(bookingService.getBookingById(1L, 99L)).thenThrow(new NotFoundException("Booking not found"));

        mockMvc.perform(get("/bookings/{bookingId}", 99L)
                        .header(REQUEST_HEADER, "1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("{\"error\": \"Booking not found\"}"));

        verify(bookingService, times(1)).getBookingById(1L, 99L);
    }

    @Test
    void getUserBookings_Success() throws Exception {
        when(bookingService.getUserBookings(1L, BookingState.ALL, 0, 10)).thenReturn(List.of(bookingResponse));

        mockMvc.perform(get("/bookings")
                        .header(REQUEST_HEADER, "1")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));

        verify(bookingService, times(1)).getUserBookings(1L, BookingState.ALL, 0, 10);
    }

    @Test
    void getUserBookings_InvalidState() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(REQUEST_HEADER, "1")
                        .param("state", "UNKNOWN")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"error\": \"Unknown state: UNKNOWN\"}"));
    }

    @Test
    void getOwnerBookings_Success() throws Exception {
        when(bookingService.getOwnerBookings(1L, BookingState.ALL, 0, 10)).thenReturn(List.of(bookingResponse));

        mockMvc.perform(get("/bookings/owner")
                        .header(REQUEST_HEADER, "1")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));

        verify(bookingService, times(1)).getOwnerBookings(1L, BookingState.ALL, 0, 10);
    }
}