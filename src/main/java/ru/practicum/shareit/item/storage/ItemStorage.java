package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    /**
     * Добавляет новую вещь
     */
    Item addItem(Item item);

    /**
     * Обновляет вещь
     */
    Item updateItem(Item user);

    /**
     * Возвращает вещь по id
     */
    Item getItem(Long id);

    /**
     * Возвращает все вещи по владельцу
     */
    List<Item> getItemsByUser(Long userId);

    /**
     * Возвращает все вещи, подходящие под поисковый запрос
     */
    List<Item> getAvailableItemsBySearch(String text);
}
