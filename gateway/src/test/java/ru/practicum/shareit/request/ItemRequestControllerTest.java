package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoGet;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
public class ItemRequestControllerTest {
    @MockBean
    private ItemRequestClient itemRequestClient;

    @Autowired
    private ItemRequestController itemRequestController;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private ItemRequestDto itemRequestDto;
    private ItemRequestDtoGet itemRequestDtoGet;
    private NewItemRequestDto newItemRequestDto;

    private static final Long REQUEST_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final String REQUEST_DESCRIPTION = "Description";
    private static final LocalDateTime CREATED = LocalDateTime.now();

    @BeforeEach
    public void setUpData() {
        newItemRequestDto = new NewItemRequestDto();
        newItemRequestDto.setDescription(REQUEST_DESCRIPTION);

        itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(REQUEST_ID);
        itemRequestDto.setDescription(REQUEST_DESCRIPTION);
        itemRequestDto.setCreated(CREATED);

        itemRequestDtoGet = new ItemRequestDtoGet();
        itemRequestDtoGet.setId(REQUEST_ID);
        itemRequestDtoGet.setDescription(REQUEST_DESCRIPTION);
        itemRequestDtoGet.setCreated(CREATED);
        itemRequestDtoGet.setItems(List.of());

    }

    @Test
    @DisplayName("При создании запроса вещи должен вернуть созданный запрос")
    void test_addItemRequest_WhenValidShouldReturnRequest() throws Exception {
        //given && when
        Mockito
                .when(itemRequestClient.addItemRequest(USER_ID, newItemRequestDto))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(itemRequestDto));

        //then
        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", USER_ID)
                        .content(mapper.writeValueAsString(newItemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(REQUEST_ID))
                .andExpect(jsonPath("$.description").value(REQUEST_DESCRIPTION));
    }

    @Test
    @DisplayName("При получении запроса по id должен вернуть запрос с вещами")
    void test_getItemRequestById_ShouldReturnRequest() throws Exception {
        //given && when
        Mockito
                .when(itemRequestClient.getItemRequest(REQUEST_ID))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(itemRequestDtoGet));

        //then
        mvc.perform(get("/requests/{id}", REQUEST_ID)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(REQUEST_ID))
                .andExpect(jsonPath("$.description").value(REQUEST_DESCRIPTION));
    }

    @Test
    @DisplayName("При получении всех своих запросов должен вернуть список запросов")
    void test_getAllItemRequestsByUser_ShouldReturnListOfRequests() throws Exception {
        //given && when
        Mockito
                .when(itemRequestClient.getAllItemRequestsByUser(USER_ID))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(itemRequestDtoGet)));

        //then
        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", USER_ID)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(REQUEST_ID))
                .andExpect(jsonPath("$[0].description").value(REQUEST_DESCRIPTION));
    }

    @Test
    @DisplayName("При получении всех чужих запросов должен вернуть список запросов")
    void test_getAllItemRequestsByOther_ShouldReturnListOfRequests() throws Exception {
        //given && when
        itemRequestDto.setRequestorId(OTHER_USER_ID);

        Mockito
                .when(itemRequestClient.getAllOtherItemRequests(USER_ID))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(itemRequestDto)));

        //then
        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", USER_ID)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(REQUEST_ID))
                .andExpect(jsonPath("$[0].description").value(REQUEST_DESCRIPTION))
                .andExpect(jsonPath("$[0].requestorId").value(OTHER_USER_ID));
    }
}