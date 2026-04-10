package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {
    /**
     * Добавляет новую вещь
     */
    ItemDto addItem(Long userId, NewItemDto item);

    /**
     * Обновляет вещь
     */
    ItemDto updateItem(Long userId, Long id, UpdateItemDto item);

    /**
     * Возвращает вещь по id
     */
    ItemDtoGetById getItemById(Long userId, Long id);

    /**
     * Возвращает все вещи пользователя, у которых он является владельцем
     */
    List<ItemDtoGet> getUserItems(Long userId);

    /**
     * Возвращает все вещи, которые содержат поисковую строку в названии или описании.
     * Вещи доступны для бронирования.
     */
    List<ItemDto> getAvailableItemsBySearch(String text);

    /**
     * Добавляет комментарий к вещи от пользователя, который бронировал её.
     */
    CommentDto addComment(Long userId, Long itemId, NewCommentDto commentDto);
}
