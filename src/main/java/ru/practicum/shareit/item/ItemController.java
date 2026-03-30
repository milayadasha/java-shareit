package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
public class ItemController {
    @Autowired
    @Qualifier("itemServiceImpl")
    private ItemService itemService;

    /**
     * Возвращает вещь по id
     */
    @GetMapping("/{id}")
    public ResponseEntity<ItemDtoGetById> getItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                      @PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemById(userId, id));
    }

    /**
     * Добавляет новую вещь
     */
    @PostMapping
    public ResponseEntity<ItemDto> addItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @Valid @RequestBody NewItemDto newItemDto) {
        newItemDto.setOwner(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.addItem(userId, newItemDto));
    }

    /**
     * Обновляет пользователя.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ItemDto> updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @PathVariable Long id,
                                              @Valid @RequestBody UpdateItemDto updateItemDto) {
        System.out.println(userId);
        return ResponseEntity.ok(itemService.updateItem(userId, id, updateItemDto));
    }

    /**
     * Возвращает список вещей пользователя
     */
    @GetMapping
    public ResponseEntity<List<ItemDtoGet>> getUserItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return ResponseEntity.ok(itemService.getUserItems(userId));
    }

    /**
     * Возвращает список вещей, которые подходят под строку поиска и доступны для аренды
     */
    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> getAvailableItemsBySearch(@RequestParam String text) {
        return ResponseEntity.ok(itemService.getAvailableItemsBySearch(text));
    }

    /**
     * Добавляет комментарий к вещи
     */
    @PostMapping("/{id}/comment")
    public ResponseEntity<CommentDto> addCommentForItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                        @PathVariable Long id,
                                                        @Valid @RequestBody NewCommentDto newCommentDto) {
        return ResponseEntity.ok(itemService.addComment(userId, id, newCommentDto));
    }
}
