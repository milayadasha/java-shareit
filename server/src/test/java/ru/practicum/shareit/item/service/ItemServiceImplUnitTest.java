package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplUnitTest {
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

    private Item item;
    private User owner;

    private static final Long ITEM_ID = 1L;
    public static final String ITEM_NAME = "Item";
    public static final String ITEM_NAME_UPDATED = "Item updated";
    public static final String ITEM_DESCRIPTION = "Item description";
    public static final String ITEM_DESCRIPTION_UPDATED = "ITEM_DESCRIPTION_UPDATED";

    public static final String SEARCH_TEXT = "ITEM";

    private static final Long COMMENT_ID = 1L;
    public static final String COMMENT_TEXT = "Comment";

    private static final Long OWNER_ID = 2L;
    public static final String OWNER_NAME = "Owner";

    private static final Long AUTHOR_ID = 3L;
    public static final String AUTHOR_NAME = "Author";

    private static final Long BOOKING_ID = 100L;
    private static final BookingStatus STATUS_APPROVED = BookingStatus.APPROVED;


    @BeforeEach
    void setUpData() {
        owner = new User();
        owner.setId(OWNER_ID);
        owner.setName(OWNER_NAME);

        item = new Item();
        item.setId(ITEM_ID);
        item.setName(ITEM_NAME);
        item.setDescription(ITEM_DESCRIPTION);
    }

    @Test
    @DisplayName("При добавлении вещи должен сохранить и вернуть вещь")
    void test_addItem_WhenAddShouldReturnItem() {
        //given
        NewItemDto newItemDto = new NewItemDto();
        newItemDto.setName(ITEM_NAME);
        newItemDto.setDescription(ITEM_DESCRIPTION);

        Mockito
                .when(userRepository.findById(OWNER_ID))
                .thenReturn(Optional.of(owner));

        Mockito
                .when(itemRepository.save(any(Item.class)))
                .thenReturn(item);

        //when
        ItemDto result = itemService.addItem(OWNER_ID, newItemDto);

        //then
        assertThat(result.getId()).isEqualTo(ITEM_ID);

        verify(itemRepository, times(1)).save(any(Item.class));
        verify(userRepository, times(2)).findById(OWNER_ID);
    }

    @Test
    @DisplayName("При обновлении вещи должен вернуть обновлённую вещь")
    void test_updateItem_WhenValidShouldReturnUpdated() {
        //given
        UpdateItemDto itemDto = new UpdateItemDto();
        itemDto.setId(ITEM_ID);
        itemDto.setName(ITEM_NAME);
        itemDto.setDescription(ITEM_DESCRIPTION);

        Item updatedItem = new Item();
        updatedItem.setId(ITEM_ID);
        updatedItem.setName(ITEM_NAME_UPDATED);
        updatedItem.setDescription(ITEM_DESCRIPTION_UPDATED);

        Mockito
                .when(itemRepository.findById(ITEM_ID))
                .thenReturn(Optional.of(item));
        Mockito
                .when(itemRepository.save(any(Item.class)))
                .thenReturn(updatedItem);

        //when
        ItemDto result = itemService.updateItem(OWNER_ID, ITEM_ID, itemDto);

        //then
        assertThat(result.getId()).isEqualTo(ITEM_ID);

        verify(itemRepository, times(1)).save(any(Item.class));
        verify(itemRepository, times(2)).findById(ITEM_ID);
    }

    @Test
    @DisplayName("При получении вещи по id должен вернуть вещь")
    void test_getItemById_WhenExistsShouldReturnOne() {
        //given
        ItemDtoGetById itemDto = new ItemDtoGetById();
        itemDto.setId(ITEM_ID);
        itemDto.setName(ITEM_NAME);
        itemDto.setDescription(ITEM_DESCRIPTION);

        Mockito
                .when(itemRepository.findById(ITEM_ID))
                .thenReturn(Optional.of(item));
        //when
        ItemDtoGetById result = itemService.getItemById(OWNER_ID, ITEM_ID);

        //then
        assertThat(result.getId()).isEqualTo(ITEM_ID);

        verify(itemRepository, times(1)).findById(ITEM_ID);
        verify(commentRepository, times(1)).findAllByItemId(ITEM_ID);
        verify(bookingRepository, times(1))
                .findLastApprovedBookingForOwner(ITEM_ID, OWNER_ID);
        verify(bookingRepository, times(1))
                .findNextApprovedBookingForOwner(ITEM_ID, OWNER_ID);
    }

    @Test
    @DisplayName("При получении всех вещей владельца должен вернуть список вещей")
    void test_getUserItems_WhenOwnerShouldReturnOne() {
        //given
        ItemDtoGet itemDto = new ItemDtoGet();
        itemDto.setId(ITEM_ID);
        itemDto.setName(ITEM_NAME);
        itemDto.setDescription(ITEM_DESCRIPTION);

        Mockito
                .when(itemRepository.findByOwnerId(OWNER_ID))
                .thenReturn(List.of(item));

        //when
        List<ItemDtoGet> result = itemService.getUserItems(OWNER_ID);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getId()).isEqualTo(ITEM_ID);

        verify(itemRepository, times(1)).findByOwnerId(OWNER_ID);
    }

    @Test
    @DisplayName("При поиске доступных вещей по тексту должен вернуть список вещей")
    void test_getAvailableItemsBySearch_ShouldReturnOne() {
        //given
        Mockito
                .when(itemRepository.getAvailableItemsBySearch(SEARCH_TEXT))
                .thenReturn(List.of(item));
        //when
        List<ItemDto> result = itemService.getAvailableItemsBySearch(SEARCH_TEXT);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getId()).isEqualTo(ITEM_ID);

        verify(itemRepository, times(1)).getAvailableItemsBySearch(SEARCH_TEXT);
    }

    @Test
    @DisplayName("При добавлении комментария должен сохранить и вернуть комментарий")
    void test_addComment_WhenCommentAddedShouldReturnWithComment() {
        //given
        NewCommentDto newCommentDto = new NewCommentDto();
        newCommentDto.setText(COMMENT_TEXT);

        User author = new User();
        author.setId(AUTHOR_ID);
        author.setName(AUTHOR_NAME);

        Comment comment = new Comment();
        comment.setId(COMMENT_ID);
        comment.setText(COMMENT_TEXT);
        comment.setAuthor(author);
        comment.setItem(item);

        Booking booking = new Booking();
        booking.setId(BOOKING_ID);
        booking.setBooker(author);
        booking.setItem(item);
        booking.setStatus(STATUS_APPROVED);

        CommentDto commentDto = new CommentDto();
        commentDto.setId(COMMENT_ID);
        commentDto.setText(COMMENT_TEXT);
        commentDto.setAuthorName(AUTHOR_NAME);

        Mockito
                .when(itemRepository.findById(ITEM_ID))
                .thenReturn(Optional.of(item));
        Mockito
                .when(userRepository.findById(AUTHOR_ID))
                .thenReturn(Optional.of(author));
        Mockito
                .when(commentRepository.save(any(Comment.class)))
                .thenReturn(comment);
        Mockito
                .when(bookingRepository.findPastByBookerIdAndItemIdAndStatusApproved(AUTHOR_ID, ITEM_ID))
                .thenReturn((List.of(booking)));

        //when
        CommentDto result = itemService.addComment(AUTHOR_ID, ITEM_ID, newCommentDto);

        //then
        assertThat(result.getId()).isEqualTo(COMMENT_ID);
        assertThat(result.getAuthorName()).isEqualTo(AUTHOR_NAME);

        verify(commentRepository, times(1)).save(any(Comment.class));
    }
}