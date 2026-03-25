package ru.practicum.shareit.item.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Slf4j
@Repository
public class InMemoryItemStorage implements ItemStorage {
    private final HashMap<Long, Item> items = new HashMap<>();

    /**
     * Добавляет новую вещь.
     * Присваивает уникальный ID и сохраняет в набор вещей.
     *
     * @param newItem объект вещи, который нужно добавить
     * @return созданная вещь с присвоенным ID
     */
    @Override
    public Item addItem(Item newItem) {
        if (newItem == null) {
            String userNotFound = "Пользователь для добавления не найден";
            log.error(userNotFound);
            throw new NotFoundException(userNotFound);
        }

        newItem.setId(getNextId());
        log.info("Вещи {} присвоен id {}", newItem.getName(), newItem.getId());
        items.put(newItem.getId(), newItem);
        log.info("Вещь {} добавлена в хранилище", newItem.getId());
        return newItem;
    }

    /**
     * Обновляет вещь.
     * Если переданная вещь существует, то обновляет вещь в наборе вещей.
     *
     * @param updatedItem объект вещи, который нужно обновить
     * @return обновлённая вещь
     */
    @Override
    public Item updateItem(Item updatedItem) {
        if (updatedItem == null) {
            String itemNotFound = "Вещь для обновления не найдена";
            log.error(itemNotFound);
            throw new NotFoundException(itemNotFound);
        }

        if (!items.containsKey(updatedItem.getId())) {
            String itemNotFound = "Вещь " + updatedItem.getId() + " для обновления не найдена в хранилище";
            log.error(itemNotFound);
            throw new NotFoundException(itemNotFound);
        }

        Item currentItem = items.get(updatedItem.getId());

        if (updatedItem.getName() == null || updatedItem.getName().isEmpty()) {
            updatedItem.setName(currentItem.getName());
        }

        if (updatedItem.getDescription() == null || updatedItem.getDescription().isEmpty()) {
            updatedItem.setDescription(currentItem.getDescription());
        }

        items.put(updatedItem.getId(), updatedItem);
        log.info("Вещь {} обновлена", updatedItem.getId());
        return updatedItem;
    }

    /**
     * Возвращает пользователя по id
     */
    @Override
    public Item getItem(Long id) {
        if (items.get(id) == null) {
            String userNotFound = "Вещь с id = " + id + " не найдена";
            log.error(userNotFound);
            throw new NotFoundException(userNotFound);
        }
        return items.get(id);
    }

    /**
     * Возвращает все вещи по владельцу
     */
    public List<Item> getItemsByUser(Long userId) {
        return items.values().stream()
                .filter(item -> item.getOwner() != null && item.getOwner().equals(userId))
                .toList();
    }

    /**
     * Возвращает все вещи, подходящие под поисковый запрос
     */
    public List<Item> getAvailableItemsBySearch(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        String checkText = text.toLowerCase();
        List<Item> resultList = items.values().stream()
                .filter(item -> item.getName().toLowerCase().contains(checkText) ||
                        item.getDescription().toLowerCase().contains(checkText))
                .filter(Item::isAvailable)
                .toList();
        if (!resultList.isEmpty()) {
            return resultList;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Генерирует следующий Id.
     * Находит максимальный текущй Id и увеличивает его.
     */
    private long getNextId() {
        long currentMaxId = items.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}