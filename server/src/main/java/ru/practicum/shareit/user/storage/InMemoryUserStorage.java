package ru.practicum.shareit.user.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Repository
public class InMemoryUserStorage implements UserStorage {
    private final HashMap<Long, User> users = new HashMap<>();

    /**
     * Возвращает всех пользователей в виде списка
     */
    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    /**
     * Возвращает пользователя по id
     */
    @Override
    public User getUser(Long id) {
        if (users.get(id) == null) {
            String userNotFound = "Пользователь с id = " + id + " не найден";
            log.error(userNotFound);
            throw new NotFoundException(userNotFound);
        }
        return users.get(id);
    }

    /**
     * Добавляет нового пользователя.
     * Присваивает уникальный ID и сохраняет в набор пользователей.
     *
     * @param newUser объект пользователя, который нужно добавить
     * @return созданный пользователь с присвоенным ID
     */
    @Override
    public User addUser(User newUser) {
        if (newUser == null) {
            String userNotFound = "Пользователь для добавления не найден";
            log.error(userNotFound);
            throw new NotFoundException(userNotFound);
        }

        newUser.setId(getNextId());
        log.info("Пользователю {} присвоен id {}", newUser.getName(), newUser.getId());
        users.put(newUser.getId(), newUser);
        log.info("Пользователь {} добавлен в хранилище", newUser.getId());
        return newUser;
    }

    /**
     * Обновляет пользователя.
     * Если переданный пользователь существует, то обновляет пользователя в наборе пользователей.
     *
     * @param updatedUser объект пользователя, который нужно обновить
     * @return обновлённый пользователь
     */
    @Override
    public User updateUser(Long id, User updatedUser) {
        if (updatedUser == null) {
            String userNotFound = "Пользователь для обновления не найден";
            log.error(userNotFound);
            throw new NotFoundException(userNotFound);
        }

        updatedUser.setId(id);
        if (!users.containsKey(updatedUser.getId())) {
            String userNotFound = "Пользователь " + updatedUser.getId() + " для обновления не найден в хранилище";
            log.error(userNotFound);
            throw new NotFoundException(userNotFound);
        }

        User currentUser = users.get(id);
        if (updatedUser.getEmail() == null || updatedUser.getEmail().isEmpty()) {
            updatedUser.setEmail(currentUser.getEmail());
        }
        if (updatedUser.getName() == null || updatedUser.getName().isEmpty()) {
            updatedUser.setName(currentUser.getName());
        }

        users.put(updatedUser.getId(), updatedUser);
        log.info("Пользователь {} обновлён", updatedUser.getId());
        return updatedUser;
    }

    /**
     * Удаляет пользователя по id
     */
    @Override
    public void deleteUser(Long id) {
        if (users.get(id) == null) {
            return;
        }
        users.remove(id);
    }

    /**
     * Генерирует следующий Id.
     * Находит максимальный текущй Id и увеличивает его.
     */
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
