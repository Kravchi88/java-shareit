package ru.practicum.shareit.item.controller;

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
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ItemController.class)
@Import(ApplicationExceptionHandler.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ItemDto itemDto = new ItemDto(1L, "Drill", "Powerful drill", true, 1L, null);
    private final ItemResponseDto itemResponseDto = new ItemResponseDto(1L, "Drill", "Powerful drill", true, null, null, List.of());
    private final CommentRequestDto commentRequestDto = new CommentRequestDto("Great item!");
    private final CommentDto commentDto = new CommentDto(1L, "Great item!", "John Doe", LocalDateTime.now());

    @Test
    void createItem_Success() throws Exception {
        when(itemService.createItem(eq(1L), any(ItemDto.class))).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(itemDto.id()))
                .andExpect(jsonPath("$.name").value(itemDto.name()))
                .andExpect(jsonPath("$.description").value(itemDto.description()));

        verify(itemService, times(1)).createItem(eq(1L), any(ItemDto.class));
    }

    @Test
    void updateItem_Success() throws Exception {
        when(itemService.updateItem(eq(1L), eq(1L), any(ItemDto.class))).thenReturn(itemDto);

        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(itemDto.name()));

        verify(itemService, times(1)).updateItem(eq(1L), eq(1L), any(ItemDto.class));
    }

    @Test
    void getItem_Success() throws Exception {
        when(itemService.getItemById(1L, 1L)).thenReturn(itemResponseDto);

        mockMvc.perform(get("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemResponseDto.id()))
                .andExpect(jsonPath("$.name").value(itemResponseDto.name()));

        verify(itemService, times(1)).getItemById(1L, 1L);
    }

    @Test
    void getItem_NotFound() throws Exception {
        when(itemService.getItemById(1L, 99L)).thenThrow(new NotFoundException("Item not found"));

        mockMvc.perform(get("/items/{itemId}", 99L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(content().string("{\"error\": \"Item not found\"}"));

        verify(itemService, times(1)).getItemById(1L, 99L);
    }

    @Test
    void getUserItems_Success() throws Exception {
        when(itemService.getUserItems(1L)).thenReturn(List.of(itemResponseDto));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(itemResponseDto.id()));

        verify(itemService, times(1)).getUserItems(1L);
    }

    @Test
    void searchItems_Success() throws Exception {
        when(itemService.searchItems("Drill")).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "Drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(itemDto.id()));

        verify(itemService, times(1)).searchItems("Drill");
    }

    @Test
    void addComment_Success() throws Exception {
        when(itemService.addComment(eq(1L), eq(1L), anyString())).thenReturn(commentDto);

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value(commentDto.text()));

        verify(itemService, times(1)).addComment(eq(1L), eq(1L), anyString());
    }
}