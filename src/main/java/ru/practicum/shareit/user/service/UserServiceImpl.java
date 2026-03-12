package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserStorage userStorage;

    /**
     * Возвращает всех пользователей
     */
    public List<UserDto> getAllUsers() {
        return userStorage.getAllUsers().stream().map(UserMapper::toUserDto).toList();
    }

    /**
     * Возвращает пользователя по id
     */
    public UserDto getUserById(Long id) {
        return UserMapper.toUserDto(userStorage.getUser(id));
    }

    /**
     * Добавляет нового пользователя
     */
    public UserDto addNewUser(NewUserDto newUserDto) {
        User newUser = UserMapper.toUser(newUserDto);
        checkIsValidUser(newUser);
        log.info("Пользователь с email {} прошёл валидацию входных данных и готов к добавлению", newUser.getEmail());
        return UserMapper.toUserDto(userStorage.addUser(newUser));
    }

    /**
     * Обновляет пользователя
     */
    public UserDto updateUser(Long id, UpdateUserDto updateUserDto) {
        User updateUser = UserMapper.toUser(updateUserDto);
        checkIsValidUser(updateUser);
        log.info("Пользователь с id {} прошёл валидацию входных данных и готов к обновлению", id);
        return UserMapper.toUserDto(userStorage.updateUser(id, updateUser));
    }

    /**
     * Удаляет пользователя
     */
    public void deleteUser(Long id) {
        userStorage.deleteUser(id);
        log.trace("Пользователь с id {} удалён", id);
    }

    /**
     * Проверяет переданного пользователя на соответствие условиям.
     * Если не удовлетворяет какой-то проверке, то выбрасывается ошибка
     */
    private void checkIsValidUser(User checkUser) throws ValidationException {
        if (checkUser == null) {
            String nullError = "Пользователь для валидации входных параметров не найден";
            log.error(nullError);
            throw new NotFoundException(nullError);
        }

        if (checkUser.getEmail() != null && !checkUser.getEmail().contains("@")) {
            String emailError = "У пользователя должен быть email в корректном формате";
            log.error(emailError);
            throw new ValidationException(emailError);
        }

        List<User> users = userStorage.getAllUsers();
        Optional<User> sameUserByEmail = users.stream()
                .filter(user -> user.getEmail().equals(checkUser.getEmail()))
                .findAny();
        if (sameUserByEmail.isPresent()) {
            String sameEmailError = "Пользователь с email " + checkUser.getEmail() + " уже существует.";
            log.error(sameEmailError);
            throw new DuplicateException(sameEmailError);
        }
    }
}
