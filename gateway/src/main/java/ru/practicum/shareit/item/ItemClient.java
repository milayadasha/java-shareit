package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.*;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    /**
     * Возвращает вещь по id
     */
    public ResponseEntity<Object> getItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @PathVariable Long id) {
        return get("/" + id, userId);
    }

    /**
     * Добавляет новую вещь
     */
    public ResponseEntity<Object> addItem(Long userId, NewItemDto newItemDto) {
        newItemDto.setOwner(userId);
        return post("", userId, newItemDto);
    }

    /**
     * Обновляет пользователя.
     */
    public ResponseEntity<Object> updateItem(Long userId, Long id, UpdateItemDto updateItemDto) {
        return patch("/" + id, userId, updateItemDto);
    }

    /**
     * Возвращает список вещей пользователя
     */
    public ResponseEntity<Object> getUserItems(Long userId) {
        return get("", userId);
    }

    /**
     * Возвращает список вещей, которые подходят под строку поиска и доступны для аренды
     */
    public ResponseEntity<Object> getAvailableItemsBySearch(String text) {
        return get("/search=" + text);
    }

    /**
     * Добавляет комментарий к вещи
     */
    public ResponseEntity<Object> addCommentForItem(Long userId, Long id, NewCommentDto newCommentDto) {
        return post("/" + id + "/comment", userId, newCommentDto);
    }
}
