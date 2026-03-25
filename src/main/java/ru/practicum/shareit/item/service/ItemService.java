package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

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
    ItemDto getItemById(Long id);

    /**
     * Возвращает все вещи пользователя, у которых он является владельцем
     */
    List<ItemDto> getUserItems(Long userId);

    /**
     * Возвращает все вещи, которые содержат поисковую строку в названии или описании.
     * Вещи доступны для бронирования.
     */
    List<ItemDto> getAvailableItemsBySearch(String text);
}
