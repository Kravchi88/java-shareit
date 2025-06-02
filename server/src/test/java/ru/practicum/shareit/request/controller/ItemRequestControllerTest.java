package ru.practicum.shareit.request.controller;

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
import ru.practicum.shareit.exception.ApplicationExceptionHandler;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ItemRequestController.class)
@Import(ApplicationExceptionHandler.class)
class ItemRequestControllerTest {

    private static final String REQUEST_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService requestService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ItemRequestDto requestDto = new ItemRequestDto("Need a drill");
    private final ItemRequestResponseDto responseDto =
            new ItemRequestResponseDto(1L, "Need a drill", LocalDateTime.now(), List.of());

    @Test
    void createRequest_Success() throws Exception {
        when(requestService.createRequest(eq(1L), any(ItemRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/requests")
                        .header(REQUEST_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(responseDto.id()))
                .andExpect(jsonPath("$.description").value(responseDto.description()));

        verify(requestService, times(1)).createRequest(eq(1L), any(ItemRequestDto.class));
    }

    @Test
    void getUserRequests_Success() throws Exception {
        when(requestService.getUserRequests(1L)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/requests")
                        .header(REQUEST_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(responseDto.id()))
                .andExpect(jsonPath("$[0].description").value(responseDto.description()));

        verify(requestService, times(1)).getUserRequests(1L);
    }

    @Test
    void getAllRequests_Success() throws Exception {
        when(requestService.getAllRequests(1L)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/requests/all")
                        .header(REQUEST_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(responseDto.id()))
                .andExpect(jsonPath("$[0].description").value(responseDto.description()));

        verify(requestService, times(1)).getAllRequests(1L);
    }

    @Test
    void getRequestById_Success() throws Exception {
        when(requestService.getRequestById(1L, 1L)).thenReturn(responseDto);

        mockMvc.perform(get("/requests/{requestId}", 1L)
                        .header(REQUEST_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.id()))
                .andExpect(jsonPath("$.description").value(responseDto.description()));

        verify(requestService, times(1)).getRequestById(1L, 1L);
    }

    @Test
    void getRequestById_NotFound() throws Exception {
        when(requestService.getRequestById(1L, 99L)).thenThrow(new NotFoundException("Request not found"));

        mockMvc.perform(get("/requests/{requestId}", 99L)
                        .header(REQUEST_HEADER, "1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("{\"error\": \"Request not found\"}"));

        verify(requestService, times(1)).getRequestById(1L, 99L);
    }
}