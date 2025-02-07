package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserStorage {
    Collection<User> findAll();
    User findById(long id);
    User save(User user);
    User updateById(long id, User user);
    void deleteById(long id);
}
