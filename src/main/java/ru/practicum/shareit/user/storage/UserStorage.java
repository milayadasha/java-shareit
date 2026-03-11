package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.User;
import java.util.List;

public interface UserStorage {
    /**
     * Возвращает всех пользователей в виде списка
     */
    List<User> getAllUsers();

    /**
     * Возвращает пользователя по id
     */
    User getUser(Long id);

    /**
     * Добавляет нового пользователя.
     */
    User addUser(User user);

    /**
     * Обновляет пользователя.
     */
    User updateUser(Long id, User user);

    /**
     * Удаляет пользователя по id
     */
    void deleteUser(Long id);
}
