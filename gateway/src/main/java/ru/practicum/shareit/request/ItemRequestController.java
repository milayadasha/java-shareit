package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    /**
     * Добавляет новый запрос на добавление вещи
     */
    @PostMapping
    public ResponseEntity<Object> addItemRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @RequestBody @Valid NewItemRequestDto newItemRequestDto) {
        return itemRequestClient.addItemRequest(userId, newItemRequestDto);
    }

    /**
     * Возвращает запрос на добавление вещи по id
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> getItemRequestById(@PathVariable Long id) {
        return itemRequestClient.getItemRequest(id);
    }

    /**
     * Возвращает запросы на добавление вещей конкретного пользователя
     */
    @GetMapping
    public ResponseEntity<Object> getAllItemRequestsByUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestClient.getAllItemRequestsByUser(userId);
    }

    /**
     * Возвращает запросы на добавление вещей всех остальных пользователей
     */
    @GetMapping("/all")
    public ResponseEntity<Object> getAllItemRequestsByOther(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestClient.getAllOtherItemRequests(userId);
    }
}
