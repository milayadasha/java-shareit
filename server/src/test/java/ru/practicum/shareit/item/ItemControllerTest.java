package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ExtendWith(MockitoExtension.class)
public class ItemControllerTest {
    @Mock
    private ItemServiceImpl itemService;

    @InjectMocks
    private ItemController itemController;

    private MockMvc mvc;

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    private ItemDto itemDto;
    private User owner;

    private static final Long ITEM_ID = 1L;
    public static final String ITEM_NAME = "Item";
    public static final String ITEM_NAME_UPDATED = "Item updated";
    public static final String ITEM_DESCRIPTION = "Item description";
    public static final String ITEM_DESCRIPTION_UPDATED = "ITEM_DESCRIPTION_UPDATED";
    private static final Boolean ITEM_AVAILABLE = true;
    private static final Boolean ITEM_NOT_AVAILABLE = false;

    public static final String SEARCH_TEXT = "ITEM";

    private static final Long COMMENT_ID = 1L;
    public static final String COMMENT_TEXT = "Comment";

    private static final Long OWNER_ID = 2L;
    public static final String OWNER_NAME = "Owner";

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(itemController).build();

        owner = new User();
        owner.setId(OWNER_ID);

        itemDto = new ItemDto();
        itemDto.setId(ITEM_ID);
        itemDto.setName(ITEM_NAME);
        itemDto.setDescription(ITEM_DESCRIPTION);
        itemDto.setAvailable(ITEM_AVAILABLE);
        itemDto.setOwner(owner.getId());
    }

    @Test
    @DisplayName("При получении вещи по id должен вернуть вещь")
    void test_getItemById_Should() throws Exception {
        //given && when
        ItemDtoGetById itemDtoById = new ItemDtoGetById();
        itemDtoById.setId(ITEM_ID);
        itemDtoById.setName(ITEM_NAME);
        itemDtoById.setDescription(ITEM_DESCRIPTION);
        itemDtoById.setAvailable(ITEM_AVAILABLE);

        Mockito
                .when(itemService.getItemById(owner.getId(), ITEM_ID))
                .thenReturn(itemDtoById);

        //then
        mvc.perform(get("/items/{itemId}", ITEM_ID)
                        .header("X-Sharer-User-Id", owner.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDto.getAvailable()));
    }

    @Test
    @DisplayName("При добавлении вещи должен сохранить и вернуть вещь")
    void test_addItem_ShouldReturnOne() throws Exception {
        //given && when
        NewItemDto newItemDto = new NewItemDto();
        newItemDto.setName(ITEM_NAME);
        newItemDto.setDescription(ITEM_DESCRIPTION);
        newItemDto.setAvailable(ITEM_AVAILABLE);

        Mockito
                .when(itemService.addItem(owner.getId(), newItemDto))
                .thenReturn(itemDto);
        //then
        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", owner.getId())
                        .content(mapper.writeValueAsString(newItemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDto.getAvailable()));
    }

    @Test
    @DisplayName("При обновлении вещи должен вернуть обновлённую вещь")
    void test_updateItem_ShouldReturnOne() throws Exception {
        //given && when
        UpdateItemDto updateItemDto = new UpdateItemDto();
        updateItemDto.setId(ITEM_ID);
        updateItemDto.setName(ITEM_NAME_UPDATED);
        updateItemDto.setDescription(ITEM_DESCRIPTION_UPDATED);
        updateItemDto.setAvailable(ITEM_NOT_AVAILABLE);

        itemDto.setId(ITEM_ID);
        itemDto.setName(ITEM_NAME_UPDATED);
        itemDto.setDescription(ITEM_DESCRIPTION_UPDATED);
        itemDto.setAvailable(ITEM_NOT_AVAILABLE);
        itemDto.setOwner(owner.getId());

        Mockito
                .when(itemService.updateItem(owner.getId(), ITEM_ID, updateItemDto))
                .thenReturn(itemDto);

        //then
        mvc.perform(patch("/items/{itemId}", ITEM_ID)
                        .header("X-Sharer-User-Id", owner.getId())
                        .content(mapper.writeValueAsString(updateItemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDto.getAvailable()));
    }

    @Test
    @DisplayName("При получении всех вещей владельца должен вернуть список вещей")
    void test_getUserItems_ShouldReturnOne() throws Exception {
        //given && when
        ItemDtoGet itemDtoGet = new ItemDtoGet();
        itemDtoGet.setId(ITEM_ID);
        itemDtoGet.setName(ITEM_NAME_UPDATED);
        itemDtoGet.setDescription(ITEM_DESCRIPTION_UPDATED);
        itemDtoGet.setAvailable(ITEM_NOT_AVAILABLE);

        Mockito
                .when(itemService.getUserItems(owner.getId()))
                .thenReturn(List.of(itemDtoGet));

        //then
        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", owner.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(List.of(itemDtoGet).size()))
                .andExpect(jsonPath("$[0].id").value(itemDtoGet.getId()))
                .andExpect(jsonPath("$[0].name").value(itemDtoGet.getName()))
                .andExpect(jsonPath("$[0].description").value(itemDtoGet.getDescription()))
                .andExpect(jsonPath("$[0].available").value(itemDtoGet.getAvailable()));
    }

    @Test
    @DisplayName("При поиске доступных вещей по тексту должен вернуть список вещей")
    void test_getAvailableItemsBySearch_ShouldReturnItem() throws Exception {
        //given && when
        Mockito
                .when(itemService.getAvailableItemsBySearch(SEARCH_TEXT))
                .thenReturn(List.of(itemDto));

        //then
        mvc.perform(get("/items/search")
                        .param("text", SEARCH_TEXT)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(List.of(itemDto).size()))
                .andExpect(jsonPath("$[0].id").value(itemDto.getId()))
                .andExpect(jsonPath("$[0].name").value(itemDto.getName()));
    }

    @Test
    @DisplayName("При добавлении комментария должен сохранить и вернуть комментарий")
    void test_addCommentForItem_ShouldReturnOne() throws Exception {
        //given
        NewCommentDto newCommentDto = new NewCommentDto();
        newCommentDto.setText(COMMENT_TEXT);

        CommentDto commentDto = new CommentDto();
        commentDto.setId(COMMENT_ID);
        commentDto.setText(COMMENT_TEXT);
        commentDto.setAuthorName(OWNER_NAME);
        commentDto.setCreated(LocalDateTime.now());

        //given && when
        Mockito
                .when(itemService.addComment(owner.getId(), ITEM_ID, newCommentDto))
                .thenReturn(commentDto);

        //then
        mvc.perform(post("/items/{itemId}/comment", ITEM_ID)
                        .header("X-Sharer-User-Id", owner.getId())
                        .content(mapper.writeValueAsString(newCommentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentDto.getId()))
                .andExpect(jsonPath("$.text").value(commentDto.getText()))
                .andExpect(jsonPath("$.authorName").value(commentDto.getAuthorName()));
    }
}