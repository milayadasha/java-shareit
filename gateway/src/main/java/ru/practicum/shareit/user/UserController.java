package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private final UserClient userClient;

    /**
     * Возвращает всех пользователей
     */
    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        return userClient.getAllUsers();
    }

    /**
     * Возвращает пользователя по id
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable long id) {
        return userClient.getUserById(id);
    }

    /**
     * Добавляет нового пользователя.
     */
    @PostMapping
    public ResponseEntity<Object> addUser(@Valid @RequestBody NewUserDto newUser) {
        return userClient.addUser(newUser);
    }

    /**
     * Обновляет пользователя.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable long id, @Valid @RequestBody UpdateUserDto updatedUser) {
        return userClient.updateUser(id, updatedUser);
    }

    /**
     * Удаляет пользователя.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable long id) {
        userClient.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}
