package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User user;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "John Doe", "john.doe@example.com");
        item = new Item(1L, "Drill", "Powerful drill", true, user, null);
        itemDto = new ItemDto(1L, "Drill", "Powerful drill", true, user.getId(), null);
    }

    @Test
    void createItem_Success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemDto createdItem = itemService.createItem(user.getId(), itemDto);

        assertThat(createdItem).isNotNull();
        assertThat(createdItem.id()).isEqualTo(item.getId());
        assertThat(createdItem.name()).isEqualTo(item.getName());

        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void createItem_UserNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.createItem(user.getId(), itemDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");

        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateItem_Success() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemDto updatedItem = new ItemDto(item.getId(), "Updated Drill", "Stronger drill", false, user.getId(), null);
        ItemDto resultItem = itemService.updateItem(user.getId(), item.getId(), updatedItem);

        assertThat(resultItem).isNotNull();
        assertThat(resultItem.name()).isEqualTo("Updated Drill");
        assertThat(resultItem.available()).isFalse();
    }

    @Test
    void updateItem_ItemNotFound() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.updateItem(user.getId(), item.getId(), itemDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Item not found");
    }

    @Test
    void updateItem_NotOwner() {
        User anotherUser = new User(2L, "Jane Doe", "jane.doe@example.com");
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemService.updateItem(anotherUser.getId(), item.getId(), itemDto))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    void getItemById_Success() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        ItemResponseDto resultItem = itemService.getItemById(user.getId(), item.getId());

        assertThat(resultItem).isNotNull();
        assertThat(resultItem.name()).isEqualTo(item.getName());
    }

    @Test
    void getItemById_ItemNotFound() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getItemById(user.getId(), item.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Item not found");
    }

    @Test
    void searchItems_Success() {
        when(itemRepository.search("Drill")).thenReturn(List.of(item));

        List<ItemDto> resultItems = itemService.searchItems("Drill");

        assertThat(resultItems).isNotEmpty();
        assertThat(resultItems.get(0).name()).isEqualTo("Drill");
    }

    @Test
    void searchItems_EmptyText() {
        List<ItemDto> resultItems = itemService.searchItems("");

        assertThat(resultItems).isEmpty();
    }

    @Test
    void addComment_Success() {
        Comment comment = new Comment(1L, "Great item!", item, user, LocalDateTime.now());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndEndBefore(anyLong(), anyLong(), any())).thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto resultComment = itemService.addComment(user.getId(), item.getId(), "Great item!");

        assertThat(resultComment).isNotNull();
        assertThat(resultComment.text()).isEqualTo("Great item!");
    }

    @Test
    void addComment_NotBooked() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndEndBefore(anyLong(), anyLong(), any())).thenReturn(false);

        assertThatThrownBy(() -> itemService.addComment(user.getId(), item.getId(), "Great item!"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("User can only comment on items they have booked");
    }

    @Test
    void updateItem_NullFields_ShouldNotChangeItem() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemDto partialUpdate = new ItemDto(null, null, null, null, null, null);
        ItemDto resultItem = itemService.updateItem(user.getId(), item.getId(), partialUpdate);

        assertThat(resultItem).isNotNull();
        assertThat(resultItem.name()).isEqualTo(item.getName());
        assertThat(resultItem.description()).isEqualTo(item.getDescription());
        assertThat(resultItem.available()).isEqualTo(item.getAvailable());
    }

    @Test
    void getUserItems_UserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getUserItems(user.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getUserItems_WithBookingsAndComments() {
        Booking lastBooking = new Booking(1L, LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(1), item, user, Booking.BookingStatus.APPROVED);
        Booking nextBooking = new Booking(2L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(5), item, user, Booking.BookingStatus.APPROVED);
        Comment comment = new Comment(1L, "Good item!", item, user, LocalDateTime.now().minusDays(2));

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwnerId(user.getId())).thenReturn(List.of(item));
        when(bookingRepository.findAllByItemIdInAndStatus(any(), eq(Booking.BookingStatus.APPROVED))).thenReturn(List.of(lastBooking, nextBooking));
        when(commentRepository.findAllByItemIdIn(any())).thenReturn(List.of(comment));

        List<ItemResponseDto> result = itemService.getUserItems(user.getId());

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).lastBooking()).isNotNull();
        assertThat(result.get(0).nextBooking()).isNotNull();
        assertThat(result.get(0).comments()).isNotEmpty();
        assertThat(result.get(0).comments().get(0).text()).isEqualTo("Good item!");
    }

    @Test
    void addComment_ItemNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.addComment(user.getId(), item.getId(), "Nice item"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Item not found");
    }

    @Test
    void addComment_UserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.addComment(user.getId(), item.getId(), "Nice item"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void searchItems_WhitespaceText_ShouldReturnEmptyList() {
        List<ItemDto> resultItems = itemService.searchItems("   ");

        assertThat(resultItems).isEmpty();
    }
}