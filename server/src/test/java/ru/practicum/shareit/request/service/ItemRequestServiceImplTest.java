package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User user;
    private ItemRequest itemRequest;
    private ItemRequestDto itemRequestDto;
    private Item item;

    @BeforeEach
    void setUp() {
        user = new User(1L, "John Doe", "john.doe@example.com");
        itemRequest = new ItemRequest(1L, "Need a drill", user, LocalDateTime.now());
        itemRequestDto = new ItemRequestDto("Need a drill");
        item = new Item(1L, "Drill", "Powerful drill", true, user, itemRequest);
    }

    @Test
    void createRequest_Success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.save(any(ItemRequest.class))).thenAnswer(invocation -> {
            ItemRequest savedRequest = invocation.getArgument(0);
            savedRequest.setId(1L);
            return savedRequest;
        });

        ItemRequestResponseDto result = itemRequestService.createRequest(user.getId(), itemRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.description()).isEqualTo("Need a drill");
        verify(itemRequestRepository).save(any(ItemRequest.class));
    }

    @Test
    void createRequest_UserNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemRequestService.createRequest(user.getId(), itemRequestDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");

        verify(itemRequestRepository, never()).save(any());
    }

    @Test
    void getUserRequests_Success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(user.getId())).thenReturn(List.of(itemRequest));
        when(itemRepository.findByRequestIdIn(List.of(itemRequest.getId()))).thenReturn(List.of(item));

        List<ItemRequestResponseDto> result = itemRequestService.getUserRequests(user.getId());

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).id()).isEqualTo(itemRequest.getId());
        assertThat(result.get(0).items()).hasSize(1);
        verify(itemRequestRepository).findByRequestorIdOrderByCreatedDesc(user.getId());
    }

    @Test
    void getUserRequests_UserNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemRequestService.getUserRequests(user.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");

        verify(itemRequestRepository, never()).findByRequestorIdOrderByCreatedDesc(anyLong());
    }

    @Test
    void getAllRequests_Success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByOtherUsers(user.getId())).thenReturn(List.of(itemRequest));
        when(itemRepository.findByRequestIdIn(List.of(itemRequest.getId()))).thenReturn(List.of(item));

        List<ItemRequestResponseDto> result = itemRequestService.getAllRequests(user.getId());

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).id()).isEqualTo(itemRequest.getId());
        assertThat(result.get(0).items()).hasSize(1);
        verify(itemRequestRepository).findAllByOtherUsers(user.getId());
    }

    @Test
    void getAllRequests_UserNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemRequestService.getAllRequests(user.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");

        verify(itemRequestRepository, never()).findAllByOtherUsers(anyLong());
    }

    @Test
    void getRequestById_Success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(itemRequest.getId())).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findByRequestIdIn(List.of(itemRequest.getId()))).thenReturn(List.of(item));

        ItemRequestResponseDto result = itemRequestService.getRequestById(user.getId(), itemRequest.getId());

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(itemRequest.getId());
        assertThat(result.items()).hasSize(1);
        verify(itemRequestRepository).findById(itemRequest.getId());
    }

    @Test
    void getRequestById_RequestNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(itemRequest.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemRequestService.getRequestById(user.getId(), itemRequest.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Request not found");

        verify(itemRepository, never()).findByRequestIdIn(anyList());
    }

    @Test
    void getRequestById_UserNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemRequestService.getRequestById(user.getId(), itemRequest.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");

        verify(itemRequestRepository, never()).findById(anyLong());
    }
}