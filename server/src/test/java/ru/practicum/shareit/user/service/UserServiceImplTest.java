package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Test User", "test@example.com");
        userDto = new UserDto(1L, "Test User", "test@example.com");
    }

    @Test
    void getAllUsers_shouldReturnUserList() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(userDto);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_shouldReturnUser_whenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(1L);

        assertThat(result).isEqualTo(userDto);
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_shouldThrowNotFoundException_whenUserNotExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with id = 1 doesn't exist");

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void createUser_shouldReturnUser_whenEmailNotExists() {
        when(userRepository.findByEmail(userDto.email())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.createUser(userDto);

        assertThat(result).isEqualTo(userDto);
        verify(userRepository, times(1)).findByEmail(userDto.email());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_shouldThrowConflictException_whenEmailAlreadyExists() {
        when(userRepository.findByEmail(userDto.email())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.createUser(userDto))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Email test@example.com already exists");

        verify(userRepository, times(1)).findByEmail(userDto.email());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_shouldUpdateNameAndEmail_whenValidRequest() {
        UserDto updatedDto = new UserDto(null, "Updated Name", "updated@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(updatedDto.email())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(new User(1L, "Updated Name", "updated@example.com"));

        UserDto result = userService.updateUser(1L, updatedDto);

        assertThat(result.name()).isEqualTo("Updated Name");
        assertThat(result.email()).isEqualTo("updated@example.com");

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByEmail(updatedDto.email());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_shouldThrowNotFoundException_whenUserNotExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(1L, userDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with id = 1 doesn't exist");

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_shouldThrowConflictException_whenEmailAlreadyExists() {
        UserDto updatedDto = new UserDto(null, "Updated Name", "updated@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(updatedDto.email())).thenReturn(Optional.of(new User(2L, "Other User", "updated@example.com")));

        assertThatThrownBy(() -> userService.updateUser(1L, updatedDto))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Email updated@example.com already exists");

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByEmail(updatedDto.email());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_shouldDeleteUser_whenExists() {
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }
}