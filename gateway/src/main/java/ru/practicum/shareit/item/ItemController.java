package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.NewCommentDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    /**
     * Возвращает вещь по id
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> getItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @PathVariable Long id) {
        return itemClient.getItemById(userId, id);
    }

    /**
     * Добавляет новую вещь
     */
    @PostMapping
    public ResponseEntity<Object> addItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @Valid @RequestBody NewItemDto newItemDto) {
        newItemDto.setOwner(userId);
        return itemClient.addItem(userId, newItemDto);
    }

    /**
     * Обновляет пользователя.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable Long id,
                                             @Valid @RequestBody UpdateItemDto updateItemDto) {
        System.out.println(userId);
        return itemClient.updateItem(userId, id, updateItemDto);
    }

    /**
     * Возвращает список вещей пользователя
     */
    @GetMapping
    public ResponseEntity<Object> getUserItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemClient.getUserItems(userId);
    }

    /**
     * Возвращает список вещей, которые подходят под строку поиска и доступны для аренды
     */
    @GetMapping("/search")
    public ResponseEntity<Object> getAvailableItemsBySearch(@RequestParam String text) {
        return itemClient.getAvailableItemsBySearch(text);
    }

    /**
     * Добавляет комментарий к вещи
     */
    @PostMapping("/{id}/comment")
    public ResponseEntity<Object> addCommentForItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                    @PathVariable Long id,
                                                    @Valid @RequestBody NewCommentDto newCommentDto) {
        return itemClient.addCommentForItem(userId, id, newCommentDto);
    }
}
