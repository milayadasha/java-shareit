package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

@Service
public class ItemRequestClient extends BaseClient {
    private static final String API_PREFIX = "/requests";

    @Autowired
    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    /**
     * Возвращает запрос на добавление вещи по id
     */
    public ResponseEntity<Object> getItemRequest(Long id) {
        return get("/" + id);
    }

    /**
     * Добавляет новый запрос на добавление вещи
     */
    public ResponseEntity<Object> addItemRequest(Long userId, NewItemRequestDto newItemRequestDto) {
        return post("", userId, newItemRequestDto);
    }

    /**
     * Возвращает запросы на добавление вещей конкретного пользователя
     */
    public ResponseEntity<Object> getAllItemRequestsByUser(Long userId) {
        return get("", userId);
    }

    /**
     * Возвращает запросы на добавление вещей всех остальных пользователей
     */
    public ResponseEntity<Object> getAllOtherItemRequests(Long userId) {
        return get("", userId);
    }
}
