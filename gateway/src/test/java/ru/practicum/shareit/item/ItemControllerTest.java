package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {
    @MockBean
    private ItemClient itemClient;

    @Autowired
    private ItemController itemController;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private ItemDto itemDto;

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
        itemDto = new ItemDto();
        itemDto.setId(ITEM_ID);
        itemDto.setName(ITEM_NAME);
        itemDto.setDescription(ITEM_DESCRIPTION);
        itemDto.setAvailable(ITEM_AVAILABLE);
        itemDto.setOwner(OWNER_ID);
    }

    @Test
    @DisplayName("При запросе вещи по id должен вернуть вещь")
    void test_getItemById_ShouldReturnOk() throws Exception {
        //given && when
        ItemDtoGetById itemDtoById = new ItemDtoGetById();
        itemDtoById.setId(ITEM_ID);
        itemDtoById.setName(ITEM_NAME);
        itemDtoById.setDescription(ITEM_DESCRIPTION);
        itemDtoById.setAvailable(ITEM_AVAILABLE);

        Mockito
                .when(itemClient.getItemById(OWNER_ID, ITEM_ID))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(itemDtoById));

        //then
        mvc.perform(get("/items/{itemId}", ITEM_ID)
                        .header("X-Sharer-User-Id", OWNER_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDto.getAvailable()));
    }

    @Test
    @DisplayName("При добавлении вещи должен вернуть созданную вещь")
    void test_addItem_ShouldReturnCreated() throws Exception {
        //given && when
        NewItemDto newItemDto = new NewItemDto();
        newItemDto.setName(ITEM_NAME);
        newItemDto.setOwner(OWNER_ID);
        newItemDto.setDescription(ITEM_DESCRIPTION);
        newItemDto.setAvailable(ITEM_AVAILABLE);

        Mockito
                .when(itemClient.addItem(OWNER_ID, newItemDto))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(itemDto));
        //then
        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", OWNER_ID)
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
    void test_updateItem_ShouldReturnOk() throws Exception {
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
        itemDto.setOwner(OWNER_ID);

        Mockito
                .when(itemClient.updateItem(OWNER_ID, ITEM_ID, updateItemDto))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(itemDto));

        //then
        mvc.perform(patch("/items/{itemId}", ITEM_ID)
                        .header("X-Sharer-User-Id", OWNER_ID)
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
    @DisplayName("При запросе всех вещей пользователя должен вернуть список вещей")
    void test_getUserItems_ShouldReturnOk() throws Exception {
        //given && when
        ItemDtoGet itemDtoGet = new ItemDtoGet();
        itemDtoGet.setId(ITEM_ID);
        itemDtoGet.setName(ITEM_NAME_UPDATED);
        itemDtoGet.setDescription(ITEM_DESCRIPTION_UPDATED);
        itemDtoGet.setAvailable(ITEM_NOT_AVAILABLE);

        Mockito
                .when(itemClient.getUserItems(OWNER_ID))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(itemDtoGet)));

        //then
        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", OWNER_ID)
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
    void test_getAvailableItemsBySearch_ShouldReturnOk() throws Exception {
        //given && when
        Mockito
                .when(itemClient.getAvailableItemsBySearch(SEARCH_TEXT))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(itemDto)));

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
    @DisplayName("При добавлении комментария к вещи должен вернуть созданный комментарий")
    void test_addCommentForItem_ShouldReturnOk() throws Exception {
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
                .when(itemClient.addCommentForItem(OWNER_ID, ITEM_ID, newCommentDto))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(commentDto));

        //then
        mvc.perform(post("/items/{itemId}/comment", ITEM_ID)
                        .header("X-Sharer-User-Id", OWNER_ID)
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
