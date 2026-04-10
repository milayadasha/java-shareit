package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImplIntegrationTest {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemService itemService;

    private User owner;
    private User booker;
    private Item item;
    Booking booking;

    public static final String ITEM_NAME = "Item";
    public static final String ITEM_NAME_UPDATED = "Item updated";
    public static final String ITEM_DESCRIPTION = "Item description";
    public static final String ITEM_DESCRIPTION_UPDATED = "ITEM_DESCRIPTION_UPDATED";
    private static final Boolean ITEM_AVAILABLE = true;
    private static final Boolean ITEM_NOT_AVAILABLE = false;

    public static final String SEARCH_TEXT = "ITEM";

    public static final String COMMENT_TEXT = "Comment";

    public static final String OWNER_NAME = "Owner";
    private static final String OWNER_EMAIL = "owner@test.com";

    private static final String BOOKER_NAME = "Booker";
    private static final String BOOKER_EMAIL = "booker@test.com";

    private static final BookingStatus STATUS_APPROVED = BookingStatus.APPROVED;

    @BeforeEach
    void setUpData() {
        owner = new User();
        owner.setName(OWNER_NAME);
        owner.setEmail(OWNER_EMAIL);
        owner = userRepository.save(owner);

        booker = new User();
        booker.setName(BOOKER_NAME);
        booker.setEmail(BOOKER_EMAIL);
        booker = userRepository.save(booker);
    }

    @Test
    @DisplayName("При добавлении вещи должен сохранить и вернуть вещь")
    void test_addItem_WhenAddShouldReturnItem() {
        //given
        NewItemDto newItemDto = new NewItemDto();
        newItemDto.setName(ITEM_NAME);
        newItemDto.setDescription(ITEM_DESCRIPTION);
        newItemDto.setAvailable(ITEM_AVAILABLE);

        //when
        ItemDto result = itemService.addItem(owner.getId(), newItemDto);

        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo(newItemDto.getName());
        assertThat(result.getDescription()).isEqualTo(newItemDto.getDescription());
        assertThat(result.getOwner()).isEqualTo(owner.getId());
        assertThat(result.getAvailable()).isEqualTo(ITEM_AVAILABLE);

        Optional<Item> savedItem = itemRepository.findById(result.getId());
        assertNotNull(savedItem, "Вещь не сохранена в БД");
    }

    @Test
    @DisplayName("При обновлении вещи должен вернуть обновлённую вещь")
    void test_updateItem_ShouldReturnUpdatedItem() {
        //given
        item = new Item();
        item.setName(ITEM_NAME);
        item.setDescription(ITEM_DESCRIPTION);
        item.setAvailable(ITEM_AVAILABLE);
        item.setOwner(owner);
        item = itemRepository.save(item);

        UpdateItemDto updateItemDto = new UpdateItemDto();
        updateItemDto.setId(item.getId());
        updateItemDto.setName(ITEM_NAME_UPDATED);
        updateItemDto.setDescription(ITEM_DESCRIPTION_UPDATED);
        updateItemDto.setAvailable(ITEM_NOT_AVAILABLE);

        //when
        ItemDto result = itemService.updateItem(owner.getId(), item.getId(), updateItemDto);

        //then
        assertThat(result.getId()).isEqualTo(item.getId());
        assertThat(result.getName()).isEqualTo(ITEM_NAME_UPDATED);
        assertThat(result.getDescription()).isEqualTo(ITEM_DESCRIPTION_UPDATED);
        assertThat(result.getAvailable()).isEqualTo(ITEM_NOT_AVAILABLE);
    }

    @Test
    @DisplayName("При получении вещи по id должен вернуть вещь")
    void test_getItemById_WhenExistsShouldReturnOne() {
        //given
        item = new Item();
        item.setName(ITEM_NAME);
        item.setDescription(ITEM_DESCRIPTION);
        item.setAvailable(ITEM_AVAILABLE);
        item.setOwner(owner);
        item = itemRepository.save(item);

        //when
        ItemDtoGetById result = itemService.getItemById(owner.getId(), item.getId());

        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo(ITEM_NAME);
        assertThat(result.getDescription()).isEqualTo(ITEM_DESCRIPTION);
        assertThat(result.getAvailable()).isEqualTo(ITEM_AVAILABLE);
    }

    @Test
    @DisplayName("При получении всех вещей владельца должен вернуть список вещей")
    void test_getUserItems_WhenOwnerShouldReturnOne() {
        //given
        item = new Item();
        item.setName(ITEM_NAME);
        item.setDescription(ITEM_DESCRIPTION);
        item.setAvailable(ITEM_AVAILABLE);
        item.setOwner(owner);
        item = itemRepository.save(item);

        //when
        List<ItemDtoGet> result = itemService.getUserItems(owner.getId());

        //then
        assertThat(result.get(0).getId()).isNotNull();
        assertThat(result.get(0).getName()).isEqualTo(ITEM_NAME);
        assertThat(result.get(0).getDescription()).isEqualTo(ITEM_DESCRIPTION);
        assertThat(result.get(0).getAvailable()).isEqualTo(ITEM_AVAILABLE);
    }

    @Test
    @DisplayName("При поиске доступных вещей по тексту должен вернуть список вещей")
    void test_getAvailableItemsBySearch_ShouldReturnOne() {
        //given
        item = new Item();
        item.setName(ITEM_NAME);
        item.setDescription(ITEM_DESCRIPTION);
        item.setAvailable(ITEM_AVAILABLE);
        item.setOwner(owner);
        item = itemRepository.save(item);

        //when
        List<ItemDto> result = itemService.getAvailableItemsBySearch(SEARCH_TEXT);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getId()).isEqualTo(item.getId());
    }

    @Test
    @DisplayName("При добавлении комментария должен сохранить и вернуть комментарий")
    void test_addComment_WhenCommentAddedShouldReturnWithComment() {
        //given
        item = new Item();
        item.setName(ITEM_NAME);
        item.setDescription(ITEM_DESCRIPTION);
        item.setAvailable(ITEM_AVAILABLE);
        item.setOwner(owner);
        item = itemRepository.save(item);

        booking = new Booking();
        booking.setItem(item);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setBooker(booker);
        booking.setStatus(STATUS_APPROVED);
        booking = bookingRepository.save(booking);

        NewCommentDto newCommentDto = new NewCommentDto(COMMENT_TEXT);

        //when
        CommentDto result = itemService.addComment(booker.getId(), item.getId(), newCommentDto);

        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getText()).isEqualTo(COMMENT_TEXT);
        assertThat(result.getAuthorName()).isEqualTo(BOOKER_NAME);
    }
}