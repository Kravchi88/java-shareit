package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserPatchDto;

import java.util.Collection;

public interface UserService {
    Collection<UserDto> getAllUsers();
    UserDto getUser(long id);
    UserDto addUser(UserDto userDto);
    UserDto updateUser(long id, UserPatchDto userDto);
    void deleteUser(long id);
}
