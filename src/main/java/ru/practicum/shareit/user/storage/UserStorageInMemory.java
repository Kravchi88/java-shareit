package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Repository
public class UserStorageInMemory implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long idCounter = 1;

    @Override
    public Collection<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User findById(long id) {
        return Optional.ofNullable(users.get(id))
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public User save(User user) {
        if (users.values().stream().anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
            throw new ConflictException("Email already exists");
        }
        user.setId(idCounter++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateById(long id, User user) {
        User existingUser = findById(id);

        if (user.getEmail() != null && !user.getEmail().equals(existingUser.getEmail())) {
            if (users.values().stream().anyMatch(u -> u.getEmail().equals(user.getEmail())
                    && !Objects.equals(u.getId(), id))) {
                throw new ConflictException("Email already exists");
            }
            existingUser.setEmail(user.getEmail());
        }

        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }

        users.put(id, existingUser);
        return existingUser;
    }

    @Override
    public void deleteById(long id) {
        users.remove(id);
    }
}
