package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserPatchDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage storage;

    @Override
    public Collection<UserDto> getAllUsers() {
        return storage.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUser(long id) {
        return UserMapper.toUserDto(storage.findById(id));
    }

    @Override
    public UserDto addUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(storage.save(user));
    }

    @Override
    public UserDto updateUser(long id, UserPatchDto userDto) {
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(storage.updateById(id, user));
    }

    @Override
    public void deleteUser(long id) {
        storage.deleteById(id);
    }
}
