package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemPatchDto;

import java.util.Collection;

public interface ItemService {

    ItemDto createItem(long userId, ItemDto itemDto);

    ItemDto updateItem(long userId, long itemId, ItemPatchDto itemDto);

    ItemDto getItem(long itemId);

    Collection<ItemDto> getUserItems(long userId);

    Collection<ItemDto> searchItems(String text);
}
