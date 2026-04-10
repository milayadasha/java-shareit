package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoGet;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceImplUnitTest {
    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User requestor;
    private ItemRequest itemRequest;
    private Item item;
    private NewItemRequestDto newItemRequestDto;

    private static final Long REQUESTOR_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long REQUEST_ID = 1L;
    private static final String REQUEST_DESCRIPTION = "Request description";

    private static final Long ITEM_ID = 1L;
    private static final String ITEM_DESCRIPTION = "Item description";

    private static final String ITEM_NAME = "Drill";
    private static final LocalDateTime CREATED = LocalDateTime.now();

    private static final String REQUESTOR_NAME = "Requestor";
    private static final String REQUESTOR_EMAIL = "requestor@example.com";
    private static final String OTHER_USER_NAME = "OtherUser";
    private static final String OTHER_USER_EMAIL = "other@example.com";

    @BeforeEach
    void setUp() {
        requestor = new User();
        requestor.setId(REQUESTOR_ID);
        requestor.setName(REQUESTOR_NAME);
        requestor.setEmail(REQUESTOR_EMAIL);

        User otherUser = new User();
        otherUser.setId(OTHER_USER_ID);
        otherUser.setName(OTHER_USER_NAME);
        otherUser.setEmail(OTHER_USER_EMAIL);

        item = new Item();
        item.setId(ITEM_ID);
        item.setName(ITEM_NAME);
        item.setDescription(ITEM_DESCRIPTION);
        item.setAvailable(true);
        item.setOwner(otherUser);

        itemRequest = new ItemRequest();
        itemRequest.setId(REQUEST_ID);
        itemRequest.setDescription(REQUEST_DESCRIPTION);
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(CREATED);
        item.setRequest(itemRequest);

        newItemRequestDto = new NewItemRequestDto();
        newItemRequestDto.setDescription(REQUEST_DESCRIPTION);
    }

    @Test
    @DisplayName("При создании запроса вещи должен сохранить и вернуть запрос")
    void test_addItemRequest_WhenValidShouldReturnRequest() {
        //given
        Mockito
                .when(userRepository.findById(REQUESTOR_ID))
                .thenReturn(Optional.of(requestor));
        Mockito
                .when(itemRequestRepository.save(any(ItemRequest.class)))
                .thenReturn(itemRequest);

        //when
        ItemRequestDto result = itemRequestService.addItemRequest(REQUESTOR_ID, newItemRequestDto);

        //then
        assertThat(result.getId()).isEqualTo(REQUEST_ID);
        assertThat(result.getDescription()).isEqualTo(REQUEST_DESCRIPTION);

        verify(userRepository, times(1)).findById(REQUESTOR_ID);
        verify(itemRequestRepository, times(1)).save(any(ItemRequest.class));
    }

    @Test
    @DisplayName("При создании запроса вещи должен вернуть ошибку, если пользователь не найден")
    void test_addItemRequest_WhenUserNotFoundShouldThrowException() {
        //given
        Mockito
                .when(userRepository.findById(REQUESTOR_ID))
                .thenReturn(Optional.empty());

        //when
        assertThatThrownBy(() -> itemRequestService.addItemRequest(REQUESTOR_ID, newItemRequestDto))
                .isInstanceOf(NotFoundException.class);

        //then
        verify(itemRequestRepository, never()).save(any(ItemRequest.class));
    }

    @Test
    @DisplayName("При получении всех своих запросов должен вернуть список запросов")
    void test_getAllUserItemRequests_ShouldReturnListWithItems() {
        //given
        Mockito
                .when(itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(REQUESTOR_ID))
                .thenReturn(List.of(itemRequest));
        Mockito
                .when(itemRepository.findAllByRequestIdIn(List.of(REQUEST_ID)))
                .thenReturn(List.of(item));

        //when
        List<ItemRequestDtoGet> result = itemRequestService.getAllUserItemRequests(REQUESTOR_ID);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getId()).isEqualTo(REQUEST_ID);
        assertThat(result.get(0).getDescription()).isEqualTo(REQUEST_DESCRIPTION);
        assertThat(result.get(0).getItems().size()).isEqualTo(1);
        assertThat(result.get(0).getItems().get(0).getId()).isEqualTo(ITEM_ID);

        verify(itemRequestRepository,
                times(1)).findAllByRequestorIdOrderByCreatedDesc(REQUESTOR_ID);
        verify(itemRepository,
                times(1)).findAllByRequestIdIn(List.of(REQUEST_ID));
    }

    @Test
    @DisplayName("При получении всех чужих запросов должен вернуть список запросов")
    void test_getAllOtherItemRequests_ShouldReturnList() {
        //given
        Mockito
                .when(itemRequestRepository.findAllRequestsByOtherUsers(OTHER_USER_ID))
                .thenReturn(List.of(itemRequest));

        //when
        List<ItemRequestDto> result = itemRequestService.getAllOtherItemRequests(OTHER_USER_ID);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getId()).isEqualTo(REQUEST_ID);
        assertThat(result.get(0).getDescription()).isEqualTo(REQUEST_DESCRIPTION);

        verify(itemRequestRepository, times(1)).findAllRequestsByOtherUsers(OTHER_USER_ID);
    }

    @Test
    @DisplayName("При получении запроса по существующему id должен вернуть запрос")
    void test_getItemRequest_WhenRequestExistsShouldReturnWithItems() {
        //given
        Mockito
                .when(itemRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findAllByRequestId(REQUEST_ID)).thenReturn(List.of(item));

        //when
        ItemRequestDtoGet result = itemRequestService.getItemRequest(REQUEST_ID);

        //then
        assertThat(result.getId()).isEqualTo(REQUEST_ID);
        assertThat(result.getDescription()).isEqualTo(REQUEST_DESCRIPTION);
        assertThat(result.getItems().size()).isEqualTo(1);
        assertThat(result.getItems().get(0).getId()).isEqualTo(ITEM_ID);

        verify(itemRequestRepository, times(1)).findById(REQUEST_ID);
        verify(itemRepository, times(1)).findAllByRequestId(REQUEST_ID);
    }

    @Test
    @DisplayName("При получении запроса по несуществующему id должен выбросить исключение")
    void test_getItemRequest_WhenRequestNotFoundShouldThrowException() {
        //given
        Mockito
                .when(itemRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.empty());

        //when
        assertThatThrownBy(() -> itemRequestService.getItemRequest(REQUEST_ID))
                .isInstanceOf(NotFoundException.class);

        //then
        verify(itemRequestRepository, times(1)).findById(REQUEST_ID);
        verify(itemRepository, never()).findAllByRequestId(any());
    }
}