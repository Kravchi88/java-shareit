package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemPatchDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        checkUserExists(userId);

        Item item = ItemMapper.toItem(itemDto, userRepository.findById(userId).orElseThrow());
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemPatchDto itemDto) {
        Item item = checkItemExists(itemId);

        if (!item.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Access denied. Only owner can update their items");
        }

        if (itemDto.name() != null) {
            item.setName(itemDto.name());
        }
        if (itemDto.description() != null) {
            item.setDescription(itemDto.description());
        }
        if (itemDto.available() != null) {
            item.setAvailable(itemDto.available());
        }

        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemResponseDto getItemById(Long userId, Long itemId) {
        Item item = checkItemExists(itemId);

        BookingShortDto lastBooking = bookingRepository.findLastBooking(itemId)
                .filter(booking -> booking.getItem().getOwner().getId().equals(userId))
                .map(BookingMapper::toBookingShortDto)
                .orElse(null);

        BookingShortDto nextBooking = bookingRepository.findNextBooking(itemId)
                .filter(booking -> booking.getItem().getOwner().getId().equals(userId))
                .map(BookingMapper::toBookingShortDto)
                .orElse(null);

        List<CommentDto> comments = commentRepository.findByItemId(itemId)
                .stream()
                .map(CommentMapper::toCommentDto)
                .toList();

        return ItemMapper.toItemResponseDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    public List<ItemResponseDto> getUserItems(Long userId) {
        checkUserExists(userId);

        List<Item> items = itemRepository.findAllByOwnerId(userId);
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();

        List<Booking> bookings = bookingRepository.findAllByItemIdInAndStatus(itemIds, Booking.BookingStatus.APPROVED);
        List<Comment> comments = commentRepository.findAllByItemIdIn(itemIds);

        Map<Long, List<Booking>> bookingsByItem = bookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));
        Map<Long, List<Comment>> commentsByItem = comments.stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

        return items.stream()
                .map(item -> {
                    List<Booking> itemBookings = bookingsByItem.getOrDefault(item.getId(), List.of());
                    List<Comment> itemComments = commentsByItem.getOrDefault(item.getId(), List.of());

                    BookingShortDto lastBooking = itemBookings.stream()
                            .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()))
                            .max(Comparator.comparing(Booking::getEnd))
                            .map(BookingMapper::toBookingShortDto)
                            .orElse(null);

                    BookingShortDto nextBooking = itemBookings.stream()
                            .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                            .min(Comparator.comparing(Booking::getStart))
                            .map(BookingMapper::toBookingShortDto)
                            .orElse(null);

                    List<CommentDto> commentsDto = itemComments.stream()
                            .map(CommentMapper::toCommentDto)
                            .toList();

                    return ItemMapper.toItemResponseDto(item, lastBooking, nextBooking, commentsDto);
                })
                .toList();
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, String text) {
        User user = checkUserExists(userId);
        Item item = checkItemExists(itemId);

        boolean hasBooking = bookingRepository.existsByBookerIdAndItemIdAndEndBefore(userId, itemId, LocalDateTime.now());
        if (!hasBooking) {
            throw new ValidationException("User can only comment on items they have booked");
        }

        Comment comment = new Comment();
        comment.setText(text);
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private User checkUserExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private Item checkItemExists(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));
    }
}