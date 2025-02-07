package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemPatchDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemDto createItem(long userId, ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto);
        userStorage.findById(userId);
        item.setOwnerId(userId);
        return ItemMapper.toItemDto(itemStorage.save(item));
    }

    @Override
    public ItemDto updateItem(long userId, long itemId, ItemPatchDto itemDto) {
        Item existingItem = itemStorage.findById(itemId);

        if (existingItem.getOwnerId() != userId) {
            throw new ForbiddenException("Access denied");
        }

        Item updatedItem = new Item(
                existingItem.getId(),
                itemDto.name() != null ? itemDto.name() : existingItem.getName(),
                itemDto.description() != null ? itemDto.description() : existingItem.getDescription(),
                itemDto.available() != existingItem.isAvailable() ? itemDto.available() : existingItem.isAvailable(),
                existingItem.getOwnerId(),
                existingItem.getRequestId()
        );

        return ItemMapper.toItemDto(itemStorage.update(updatedItem));
    }

    @Override
    public ItemDto getItem(long itemId) {
        return ItemMapper.toItemDto(itemStorage.findById(itemId));
    }

    @Override
    public Collection<ItemDto> getUserItems(long userId) {
        return itemStorage.findByOwnerId(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> searchItems(String text) {
        return itemStorage.searchItems(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}