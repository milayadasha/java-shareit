package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoGet;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ItemRequestControllerTest {
    @Mock
    private ItemRequestServiceImpl itemRequestService;

    @InjectMocks
    private ItemRequestController itemRequestController;

    private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    private ItemRequestDto itemRequestDto;
    private ItemRequestDtoGet itemRequestDtoGet;
    private NewItemRequestDto newItemRequestDto;
    private User user;

    private static final Long REQUEST_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final String REQUEST_DESCRIPTION = "Description";
    private static final LocalDateTime CREATED = LocalDateTime.now();

    @BeforeEach
    public void setUpData() {
        mvc = MockMvcBuilders.standaloneSetup(itemRequestController).build();

        user = new User();
        user.setId(USER_ID);

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
    @DisplayName("При создании запроса вещи должен сохранить и вернуть запрос")
    void test_addItemRequest_WhenValidShouldReturnRequest() throws Exception {
        //given && when
        Mockito
                .when(itemRequestService.addItemRequest(USER_ID, newItemRequestDto))
                .thenReturn(itemRequestDto);

        //then
        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", user.getId())
                        .content(mapper.writeValueAsString(newItemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(REQUEST_ID))
                .andExpect(jsonPath("$.description").value(REQUEST_DESCRIPTION));
    }

    @Test
    void test_getItemRequestById_ShouldReturnRequest() throws Exception {
        //given && when
        Mockito
                .when(itemRequestService.getItemRequest(REQUEST_ID))
                .thenReturn(itemRequestDtoGet);

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
                .when(itemRequestService.getAllUserItemRequests(USER_ID))
                .thenReturn(List.of(itemRequestDtoGet));

        //then
        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", user.getId())
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
                .when(itemRequestService.getAllOtherItemRequests(USER_ID))
                .thenReturn(List.of(itemRequestDto));

        //then
        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", user.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(REQUEST_ID))
                .andExpect(jsonPath("$[0].description").value(REQUEST_DESCRIPTION))
                .andExpect(jsonPath("$[0].requestorId").value(OTHER_USER_ID));
    }
}