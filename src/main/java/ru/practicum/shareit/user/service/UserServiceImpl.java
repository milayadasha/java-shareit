package ru.practicum.shareit.user.service;

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
        return UserMapper.toUserDto(userStorage.addUser(newUser));
    }

    /**
     * Обновляет пользователя
     */
    public UserDto updateUser(Long id, UpdateUserDto updateUserDto) {
        User updateUser = UserMapper.toUser(updateUserDto);
        checkIsValidUser(updateUser);
        return UserMapper.toUserDto(userStorage.updateUser(id, updateUser));
    }

    /**
     * Удаляет пользователя
     */
    public void deleteUser(Long id) {
        userStorage.deleteUser(id);
    }

    /**
     * Проверяет переданного пользователя на соответствие условиям.
     * Если не удовлетворяет какой-то проверке, то выбрасывается ошибка
     */
    private void checkIsValidUser(User checkUser) throws ValidationException {
        if (checkUser == null) {
            throw new NotFoundException("Пользователь для валидации входных параметров не найден");
        }

        if (checkUser.getEmail() != null && !checkUser.getEmail().contains("@")) {
            throw new ValidationException("У пользователя должен быть email в корректном формате");
        }

        List<User> users = userStorage.getAllUsers();
        Optional<User> sameUserByEmail = users.stream()
                .filter(user -> user.getEmail().equals(checkUser.getEmail()))
                .findAny();
        if (sameUserByEmail.isPresent()) {
            throw new DuplicateException("Пользователь с email " + checkUser.getEmail() + " уже существует.");
        }
    }
}
