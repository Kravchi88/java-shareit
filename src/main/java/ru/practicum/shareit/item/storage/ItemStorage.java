package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemStorage {

    Item save(Item item);

    Item update(Item item);

    Item findById(long itemId);

    Collection<Item> findByOwnerId(long ownerId);

    Collection<Item> searchItems(String text);
}
