package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.isAvailable(),
                item.getOwnerId(),
                item.getRequestId()
        );
    }

    public static Item toItem(ItemDto itemDto) {
        return new Item(
                itemDto.id(),
                itemDto.name(),
                itemDto.description(),
                itemDto.available(),
                itemDto.ownerId(),
                itemDto.requestId()
        );
    }
}