package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImplIntegrationTest {
    private final UserService userService;
    private final UserRepository userRepository;

    private NewUserDto newUserDto;
    private UpdateUserDto updateUserDto;

    private static final String USER_NAME = "User";
    private static final String USER_EMAIL = "user@example.com";
    private static final String UPDATED_NAME = "Updated user";
    private static final String UPDATED_EMAIL = "updateuser@example.com";
    private static final String INVALID_EMAIL = "invalid-email";

    @BeforeEach
    void setUp() {
        newUserDto = new NewUserDto();
        newUserDto.setName(USER_NAME);
        newUserDto.setEmail(USER_EMAIL);

        updateUserDto = new UpdateUserDto();
        updateUserDto.setName(UPDATED_NAME);
        updateUserDto.setEmail(UPDATED_EMAIL);
    }

    @Test
    @DisplayName("При создании пользователя должен сохранить и вернуть пользователя")
    void test_addNewUser_ShouldReturnUser() {
        //given && when
        UserDto result = userService.addNewUser(newUserDto);

        // then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo(USER_NAME);
        assertThat(result.getEmail()).isEqualTo(USER_EMAIL);

        UserDto saved = userService.getUserById(result.getId());
        assertThat(saved.getName()).isEqualTo(USER_NAME);
        assertThat(saved.getEmail()).isEqualTo(USER_EMAIL);
    }

    @Test
    @DisplayName("При создании пользователя с невалидным email должен выбросить исключение")
    void test_addNewUser_WhenInvalidEmailShouldThrowsException() {
        //given && when
        newUserDto.setEmail(INVALID_EMAIL);

        //then
        assertThatThrownBy(() -> userService.addNewUser(newUserDto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("При создании пользователя с уже существующим email должен выбросить исключение")
    void test_addNewUser_WhenEmailExistsShouldThrowsException() {
        //given && when
        userService.addNewUser(newUserDto);

        NewUserDto duplicateUserDto = new NewUserDto();
        duplicateUserDto.setName(USER_NAME);
        duplicateUserDto.setEmail(USER_EMAIL);

        //then
        assertThatThrownBy(() -> userService.addNewUser(duplicateUserDto))
                .isInstanceOf(DuplicateException.class);
    }

    @Test
    @DisplayName("При получении пользователя по id должен вернуть пользователя")
    void test_getUserById_WhenUserExistsShouldReturnOne() {
        //given
        UserDto user = userService.addNewUser(newUserDto);

        //when
        UserDto result = userService.getUserById(user.getId());

        //then
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getName()).isEqualTo(user.getName());
        assertThat(result.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("При получении всех пользователей должен вернуть список пользователей")
    void test_getAllUsers_ShouldReturnListOfUsers() {
        //given
        userService.addNewUser(newUserDto);

        //when
        List<UserDto> result = userService.getAllUsers();

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getName()).isEqualTo(USER_NAME);
        assertThat(result.get(0).getEmail()).isEqualTo(USER_EMAIL);
    }

    @Test
    @DisplayName("При обновлении пользователя должен вернуть обновлённого пользователя")
    void test_updateUser_WhenValidDataShouldUpdateUser() {
        //given
        UserDto user = userService.addNewUser(newUserDto);

        //when
        UserDto result = userService.updateUser(user.getId(), updateUserDto);

        //then
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getName()).isEqualTo(UPDATED_NAME);
        assertThat(result.getEmail()).isEqualTo(UPDATED_EMAIL);

        User updated = userRepository.findById(user.getId()).get();
        assertThat(updated.getName()).isEqualTo(UPDATED_NAME);
        assertThat(updated.getEmail()).isEqualTo(UPDATED_EMAIL);
    }

    @Test
    @DisplayName("При удалении пользователя должен удалить его из базы данных")
    void test_deleteUser_ShouldRemoveFromDatabase() {
        //given
        UserDto user = userService.addNewUser(newUserDto);

        //when
        userService.deleteUser(user.getId());

        //then
        assertThatThrownBy(() -> userService.getUserById(user.getId()))
                .isInstanceOf(NotFoundException.class);
    }
}