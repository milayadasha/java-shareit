package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Component
public interface UserService {
    /**
     * Возвращает всех пользователей
     */
    List<UserDto> getAllUsers();

    /**
     * Возвращает пользователя по id
     */
    UserDto getUserById(Long id);

    /**
     * Добавляет нового пользователя
     */
    UserDto addNewUser(NewUserDto newUserDto);

    /**
     * Обновляет пользователя
     */
    UserDto updateUser(Long id, UpdateUserDto updateUserDto);

    /**
     * Удаляет пользователя
     */
    void deleteUser(Long id);
}
