package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoGet;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceImplIntegrationTest {
    private final ItemRequestService itemRequestService;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private User requestor;
    private User otherUser;
    private Item item;

    private NewItemRequestDto newItemRequestDto;

    private static final String REQUESTOR_NAME = "Requestor";
    private static final String REQUESTOR_EMAIL = "requestor@example.com";
    private static final String OTHER_USER_NAME = "OtherUser";
    private static final String OTHER_USER_EMAIL = "other@example.com";
    private static final String REQUEST_DESCRIPTION = "Request description";
    private static final String ITEM_NAME = "Item";
    private static final String ITEM_DESCRIPTION = "Item description";
    private static final Long OTHER_ITEM_ID = 999L;

    @BeforeEach
    void setUp() {
        requestor = new User();
        requestor.setName(REQUESTOR_NAME);
        requestor.setEmail(REQUESTOR_EMAIL);
        requestor = userRepository.save(requestor);

        otherUser = new User();
        otherUser.setName(OTHER_USER_NAME);
        otherUser.setEmail(OTHER_USER_EMAIL);
        otherUser = userRepository.save(otherUser);

        item = new Item();
        item.setName(ITEM_NAME);
        item.setDescription(ITEM_DESCRIPTION);
        item.setAvailable(true);
        item.setOwner(otherUser);
        item = itemRepository.save(item);

        newItemRequestDto = new NewItemRequestDto();
        newItemRequestDto.setDescription(REQUEST_DESCRIPTION);
    }

    @Test
    @DisplayName("При создании запроса вещи должен сохранить и вернуть запрос")
    void test_addItemRequest_ShouldReturnRequest() {
        //given && when
        ItemRequestDto result = itemRequestService.addItemRequest(requestor.getId(), newItemRequestDto);

        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getDescription()).isEqualTo(REQUEST_DESCRIPTION);
        assertThat(result.getCreated()).isNotNull();
    }

    @Test
    @DisplayName("При получении всех своих запросов должен вернуть список запросов")
    void test_getAllUserItemRequests_ShouldReturnListOfRequests() {
        //given
        itemRequestService.addItemRequest(requestor.getId(), newItemRequestDto);

        //when
        List<ItemRequestDtoGet> result = itemRequestService.getAllUserItemRequests(requestor.getId());

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getDescription()).isEqualTo(REQUEST_DESCRIPTION);
    }

    @Test
    @DisplayName("При получении всех чужих запросов должен вернуть список запросов")
    void test_getAllOtherItemRequests_shouldReturnListOfOtherRequests() {
        //given
        itemRequestService.addItemRequest(requestor.getId(), newItemRequestDto);

        //when
        List<ItemRequestDto> result = itemRequestService.getAllOtherItemRequests(otherUser.getId());

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getDescription()).isEqualTo(REQUEST_DESCRIPTION);
    }

    @Test
    @DisplayName("При получении запроса по несуществующему id должен выбросить исключение")
    void test_getItemRequest_ShouldReturnRequestWithItems() {
        //given
        ItemRequestDto savedRequest = itemRequestService.addItemRequest(requestor.getId(), newItemRequestDto);
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(savedRequest.getId());
        itemRequest.setRequestor(requestor);

        item.setRequest(itemRequest);
        itemRepository.save(item);

        //when
        ItemRequestDtoGet result = itemRequestService.getItemRequest(savedRequest.getId());

        //then
        assertThat(result.getId()).isEqualTo(savedRequest.getId());
        assertThat(result.getDescription()).isEqualTo(REQUEST_DESCRIPTION);
        assertThat(result.getItems().get(0).getId()).isEqualTo(item.getId());
        assertThat(result.getItems().get(0).getName()).isEqualTo(ITEM_NAME);
    }

    @Test
    @DisplayName("При получении запроса по несуществующему id должен выбросить исключение")
    void test_getItemRequest_WhenRequestNotFoundShouldThrowException() {
        //given && when && then
        assertThatThrownBy(() -> itemRequestService.getItemRequest(OTHER_ITEM_ID))
                .isInstanceOf(NotFoundException.class);
    }
}