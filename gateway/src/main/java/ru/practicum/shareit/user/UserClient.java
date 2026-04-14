package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;

@Service
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    /**
     * Возвращает всех пользователей
     */
    public ResponseEntity<Object> getAllUsers() {
        return get("");
    }

    /**
     * Возвращает пользователя по id
     */
    public ResponseEntity<Object> getUserById(Long id) {
        return get("/" + id);
    }

    /**
     * Добавляет нового пользователя.
     */
    public ResponseEntity<Object> addUser(NewUserDto newUserDto) {
        return post("", newUserDto);
    }

    /**
     * Обновляет пользователя.
     */
    public ResponseEntity<Object> updateUser(Long id, UpdateUserDto updatedUserDto) {
        return patch("/" + id, updatedUserDto);
    }

    /**
     * Удаляет пользователя.
     */
    public void deleteUser(Long id) {
        delete("/" + id);
    }
}
