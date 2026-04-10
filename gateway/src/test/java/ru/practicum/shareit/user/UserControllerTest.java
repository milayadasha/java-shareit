package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @Mock
    private UserClient userClient;

    @InjectMocks
    private UserController userController;

    private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    private NewUserDto newUserDto;
    private UserDto userDto;

    private static final Long USER_ID = 1L;
    private static final String USER_NAME = "User";
    private static final String USER_EMAIL = "user@example.com";
    private static final String UPDATED_NAME = "Updated user";
    private static final String UPDATED_EMAIL = "updateuser@example.com";
    private static final String INVALID_EMAIL = "example.com";

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(userController).build();

        newUserDto = new NewUserDto();
        newUserDto.setName(USER_NAME);
        newUserDto.setEmail(USER_EMAIL);

        userDto = new UserDto();
        userDto.setId(USER_ID);
        userDto.setName(USER_NAME);
        userDto.setEmail(USER_EMAIL);
    }

    @Test
    @DisplayName("При создании пользователя должен вернуть созданного пользователя")
    void addUser_ShouldReturnCreated() throws Exception {
        //given && when
        Mockito
                .when(userClient.addUser(any(NewUserDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(userDto));

        //then
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(newUserDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value(USER_NAME));

        verify(userClient, times(1)).addUser(any(NewUserDto.class));
    }

    @Test
    @DisplayName("При создании пользователя с невалидным email должен вернуть ошибку 400")
    void addUser_whenInvalidEmail_shouldReturnBadRequest() throws Exception {
        //given && when
        newUserDto.setEmail(INVALID_EMAIL);

        //then
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(newUserDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).addUser(any());
    }

    @Test
    @DisplayName("При получении всех пользователей должен вернуть список пользователей")
    void test_getAllUsers_ShouldReturnListOfUsers() throws Exception {
        //given && when
        Mockito
                .when(userClient.getAllUsers())
                .thenReturn(ResponseEntity.ok(List.of(userDto)));

        //then
        mvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(USER_ID))
                .andExpect(jsonPath("$[0].name").value(USER_NAME));

        verify(userClient, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("При получении пользователя по id должен вернуть пользователя")
    void test_getUserById_ShouldReturnUser() throws Exception {
        //given && when
        Mockito
                .when(userClient.getUserById(USER_ID))
                .thenReturn(ResponseEntity.ok(userDto));

        mvc.perform(get("/users/{id}", USER_ID)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.name").value(USER_NAME));

        verify(userClient, times(1)).getUserById(USER_ID);
    }

    @Test
    @DisplayName("При получении пользователя по несуществующему id должен вернуть ошибку 404")
    void test_getUserById_WhenUserNotFoundShouldReturnNotFound() throws Exception {
        //given && when
        Mockito
                .when(userClient.getUserById(USER_ID))
                .thenReturn(ResponseEntity.notFound().build());
        //then
        mvc.perform(get("/users/{id}", USER_ID)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userClient, times(1)).getUserById(USER_ID);
    }

    @Test
    @DisplayName("При обновлении пользователя должен вернуть обновлённого пользователя")
    void test_updateUser_shouldReturnOk() throws Exception {
        //given && when
        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setName(UPDATED_NAME);
        updateUserDto.setEmail(UPDATED_EMAIL);

        UserDto updatedUserDto = new UserDto();
        updatedUserDto.setId(USER_ID);
        updatedUserDto.setName(UPDATED_NAME);
        updatedUserDto.setEmail(UPDATED_EMAIL);

        Mockito
                .when(userClient.updateUser(eq(USER_ID), any(UpdateUserDto.class)))
                .thenReturn(ResponseEntity.ok(updatedUserDto));

        //then
        mvc.perform(patch("/users/{id}", USER_ID)
                        .content(mapper.writeValueAsString(updateUserDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.name").value(UPDATED_NAME));

        verify(userClient, times(1)).updateUser(eq(USER_ID), any(UpdateUserDto.class));
    }

    @Test
    @DisplayName("При удалении пользователя должен вернуть статус 200")
    void test_deleteUser_ShouldReturnOk() throws Exception {
        //given && when
        Mockito
                .doNothing()
                .when(userClient).deleteUser(USER_ID);

        //then
        mvc.perform(delete("/users/{id}", USER_ID))
                .andExpect(status().isOk());

        verify(userClient, times(1)).deleteUser(USER_ID);
    }
}