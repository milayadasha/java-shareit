package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplUnitTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;
    private NewUserDto newUserDto;
    private UpdateUserDto updateUserDto;

    private static final Long USER_ID = 1L;
    private static final String USER_NAME = "User";
    private static final String USER_EMAIL = "user@example.com";
    private static final String UPDATED_NAME = "Updated user";
    private static final String UPDATED_EMAIL = "updateuser@example.com";
    private static final String INVALID_EMAIL = "invalid-email";
    private static final String EXISTING_EMAIL = "existing@example.com";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(USER_ID);
        user.setName(USER_NAME);
        user.setEmail(USER_EMAIL);

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
    void test_getAllUsers_shouldReturnListOfUsers() {
        //given
        Mockito
                .when(userRepository.findAll())
                .thenReturn(List.of(user));

        //when
        List<UserDto> result = userService.getAllUsers();

        //then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(USER_ID);
        assertThat(result.get(0).getName()).isEqualTo(USER_NAME);
        assertThat(result.get(0).getEmail()).isEqualTo(USER_EMAIL);

        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("При получении всех пользователей должен вернуть пустой список, если нет пользователей")
    void test_getAllUsers_WhenNoUsersShouldReturnEmptyList() {
        //given
        Mockito
                .when(userRepository.findAll())
                .thenReturn(List.of());

        //when
        List<UserDto> result = userService.getAllUsers();

        //then
        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("При создании пользователя с уже существующим email должен выбросить исключение")
    void test_getUserById_WhenUserExistsShouldReturnUser() {
        Mockito
                .when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(USER_ID);

        assertThat(result.getId()).isEqualTo(USER_ID);
        assertThat(result.getName()).isEqualTo(USER_NAME);
        assertThat(result.getEmail()).isEqualTo(USER_EMAIL);

        verify(userRepository, times(1)).findById(USER_ID);
    }

    @Test
    @DisplayName("При получении пользователя по id должен вернуть ошибку, если пользователя нет")
    void test_getUserById_WhenUserNotFoundShouldThrowException() {
        //given && when
        Mockito
                .when(userRepository.findById(USER_ID))
                .thenReturn(Optional.empty());

        //then
        assertThatThrownBy(() -> userService.getUserById(USER_ID))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository, times(1)).findById(USER_ID);
    }

    @Test
    @DisplayName("При создании пользователя должен сохранить и вернуть пользователя")
    void test_addNewUser_WhenValidShouldReturnUser() {
        //given
        Mockito
                .when(userRepository.save(any(User.class)))
                .thenReturn(user);

        //when
        UserDto result = userService.addNewUser(newUserDto);

        //then
        assertThat(result.getId()).isEqualTo(USER_ID);
        assertThat(result.getName()).isEqualTo(USER_NAME);
        assertThat(result.getEmail()).isEqualTo(USER_EMAIL);

        verify(userRepository, times(1)).findAll();
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("При создании пользователя с невалидным email должен выбросить исключение")
    void test_addNewUser_WhenInvalidEmailShouldThrowException() {
        //given && when
        newUserDto.setEmail(INVALID_EMAIL);

        //then
        assertThatThrownBy(() -> userService.addNewUser(newUserDto))
                .isInstanceOf(ValidationException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("При создании пользователя с уже существующим email должен выбросить исключение")
    void test_addNewUser_WhenEmailAlreadyExistsShouldThrowException() {
        //when
        User existingUser = new User();
        existingUser.setEmail(EXISTING_EMAIL);
        newUserDto.setEmail(EXISTING_EMAIL);

        Mockito
                .when(userRepository.findAll())
                .thenReturn(List.of(existingUser));

        assertThatThrownBy(() -> userService.addNewUser(newUserDto))
                .isInstanceOf(DuplicateException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("При обновлении пользователя должен вернуть обновлённого пользователя")
    void test_updateUser_WhenValidDataShouldUpdate() {
        //given
        User updatedUser = new User();
        updatedUser.setId(USER_ID);
        updatedUser.setName(UPDATED_NAME);
        updatedUser.setEmail(UPDATED_EMAIL);

        Mockito
                .when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));
        Mockito
                .when(userRepository.findAll())
                .thenReturn(List.of(user));
        Mockito
                .when(userRepository.save(any(User.class)))
                .thenReturn(updatedUser);

        //when
        UserDto result = userService.updateUser(USER_ID, updateUserDto);

        //then
        assertThat(result.getId()).isEqualTo(USER_ID);
        assertThat(result.getName()).isEqualTo(UPDATED_NAME);
        assertThat(result.getEmail()).isEqualTo(UPDATED_EMAIL);

        verify(userRepository, times(1)).findById(USER_ID);
        verify(userRepository, times(1)).findAll();
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("При удалении пользователя должен удалить его из базы данных")
    void test_deleteUser_ShouldDeleteById() {
        //given
        Mockito
                .doNothing()
                .when(userRepository).deleteById(USER_ID);

        //when
        userService.deleteUser(USER_ID);

        //then
        verify(userRepository, times(1)).deleteById(USER_ID);
    }
}