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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    private UserDto userDto;
    private NewUserDto newUserDto;
    private UpdateUserDto updateUserDto;

    private static final Long USER_ID = 1L;
    private static final String USER_NAME = "User";
    private static final String USER_EMAIL = "user@example.com";
    private static final String UPDATED_NAME = "Updated user";
    private static final String UPDATED_EMAIL = "updateuser@example.com";

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(userController).build();

        userDto = new UserDto();
        userDto.setId(USER_ID);
        userDto.setName(USER_NAME);
        userDto.setEmail(USER_EMAIL);

        newUserDto = new NewUserDto();
        newUserDto.setName(USER_NAME);
        newUserDto.setEmail(USER_EMAIL);

        updateUserDto = new UpdateUserDto();
        updateUserDto.setName(UPDATED_NAME);
        updateUserDto.setEmail(UPDATED_EMAIL);
    }

    @Test
    @DisplayName("При получении всех пользователей должен вернуть список пользователей")
    void test_getAllUsers_ShouldReturnListOfUsers() throws Exception {
        //given && when
        Mockito
                .when(userService.getAllUsers())
                .thenReturn(List.of(userDto));

        //then
        mvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(USER_ID))
                .andExpect(jsonPath("$[0].name").value(USER_NAME))
                .andExpect(jsonPath("$[0].email").value(USER_EMAIL));
    }

    @Test
    @DisplayName("При получении пользователя по id должен вернуть пользователя")
    void test_getUserById_shouldReturnUser() throws Exception {
        //given && when
        Mockito
                .when(userService.getUserById(USER_ID))
                .thenReturn(userDto);

        mvc.perform(get("/users/{id}", USER_ID)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.name").value(USER_NAME))
                .andExpect(jsonPath("$.email").value(USER_EMAIL));
    }

    @Test
    @DisplayName("При создании пользователя должен сохранить и вернуть пользователя")
    void test_addUser_ShouldReturnUser() throws Exception {
        //given && when
        Mockito
                .when(userService.addNewUser(any(NewUserDto.class))).thenReturn(userDto);

        //then
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(newUserDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.name").value(USER_NAME))
                .andExpect(jsonPath("$.email").value(USER_EMAIL));
    }

    @Test
    @DisplayName("При обновлении пользователя должен вернуть обновлённого пользователя")
    void test_updateUser_ShouldReturnUser() throws Exception {
        //given && when
        Mockito
                .when(userService.updateUser(USER_ID, updateUserDto))
                .thenReturn(userDto);

        //then
        mvc.perform(patch("/users/{id}", USER_ID)
                        .content(mapper.writeValueAsString(updateUserDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.name").value(USER_NAME))
                .andExpect(jsonPath("$.email").value(USER_EMAIL));
    }

    @Test
    @DisplayName("При удалении пользователя должен удалить его из базы данных")
    void test_deleteUser_ShouldDeleteUser() throws Exception {
        //given && when
        Mockito
                .doNothing()
                .when(userService).deleteUser(USER_ID);

        //then
        mvc.perform(delete("/users/{id}", USER_ID)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}