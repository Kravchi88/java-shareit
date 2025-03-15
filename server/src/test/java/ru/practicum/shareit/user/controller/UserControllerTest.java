package ru.practicum.shareit.user.controller;

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
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(UserController.class)
@Import(ApplicationExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UserDto userDto = new UserDto(1L, "John Doe", "john.doe@example.com");

    @Test
    void getAllUsers_Success() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(userDto));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(userDto.id()))
                .andExpect(jsonPath("$[0].name").value(userDto.name()))
                .andExpect(jsonPath("$[0].email").value(userDto.email()));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getUserById_Success() throws Exception {
        when(userService.getUserById(userDto.id())).thenReturn(userDto);

        mockMvc.perform(get("/users/{userId}", userDto.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.id()))
                .andExpect(jsonPath("$.name").value(userDto.name()))
                .andExpect(jsonPath("$.email").value(userDto.email()));

        verify(userService, times(1)).getUserById(userDto.id());
    }

    @Test
    void getUserById_NotFound() throws Exception {
        when(userService.getUserById(99L)).thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/users/{userId}", 99L))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(99L);
    }

    @Test
    void createUser_Success() throws Exception {
        when(userService.createUser(any(UserDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userDto.id()))
                .andExpect(jsonPath("$.name").value(userDto.name()))
                .andExpect(jsonPath("$.email").value(userDto.email()));

        verify(userService, times(1)).createUser(any(UserDto.class));
    }

    @Test
    void createUser_Conflict() throws Exception {
        when(userService.createUser(any(UserDto.class)))
                .thenThrow(new ConflictException("Email already exists"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isConflict());

        verify(userService, times(1)).createUser(any(UserDto.class));
    }

    @Test
    void updateUser_Success() throws Exception {
        UserDto updatedUser = new UserDto(userDto.id(), "John Updated", "updated@example.com");
        when(userService.updateUser(eq(userDto.id()), any(UserDto.class))).thenReturn(updatedUser);

        mockMvc.perform(patch("/users/{userId}", userDto.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        verify(userService, times(1)).updateUser(eq(userDto.id()), any(UserDto.class));
    }

    @Test
    void deleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUser(userDto.id());

        mockMvc.perform(delete("/users/{userId}", userDto.id()))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(userDto.id());
    }
}