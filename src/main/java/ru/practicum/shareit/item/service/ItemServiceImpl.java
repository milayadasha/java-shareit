package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AuthorizationException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ItemStorage itemStorage;

    @Autowired
    private UserStorage userStorage;

    /**
     * Добавляет новую вещь
     */
    public ItemDto addItem(Long userId, NewItemDto itemDto) {
        Item newItem = ItemMapper.toItem(itemDto);
        newItem.setOwner(userId);
        checkIsValidItem(newItem);
        return ItemMapper.toItemDto(itemStorage.addItem(newItem));
    }

    /**
     * Обновляет вещь
     */
    public ItemDto updateItem(Long userId, Long id, UpdateItemDto itemDto) {
        checkIsValidUser(userId, id);

        Item updateItem = ItemMapper.toItem(itemDto);
        updateItem.setId(id);
        checkIsValidItem(updateItem);

        return ItemMapper.toItemDto(itemStorage.updateItem(updateItem));
    }

    /**
     * Возвращает вещь по id
     */
    public ItemDto getItemById(Long id) {
        return ItemMapper.toItemDto(itemStorage.getItem(id));
    }

    /**
     * Возвращает все вещи пользователя, у которых он является владельцем
     */
    public List<ItemDto> getUserItems(Long userId) {
        return itemStorage.getItemsByUser(userId).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    /**
     * Возвращает все вещи, которые содержат поисковую строку в названии или описании.
     * Вещи доступны для бронирования.
     */
    public List<ItemDto> getAvailableItemsBySearch(String text) {
        return itemStorage.getAvailableItemsBySearch(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    /**
     * Валидация вещи.
     * Выбрасывает ошибку, если не прошло какое-то условие.
     */
    private void checkIsValidItem(Item item) {
        if (item.getOwner() != null && userStorage.getUser(item.getOwner()) == null) {
            throw new NotFoundException("Пользователь с id" + item.getOwner() + " для добавления вещи не найден");
        }

        if (item.getName() != null && item.getName().isEmpty()) {
            throw new ValidationException("Имя вещи не должно быть пустым");
        }

        if (item.getDescription() != null && item.getDescription().isEmpty()) {
            throw new ValidationException("Описание вещи не должно быть пустым");
        }
    }

    /**
     * Валидация пользователя, который запрашивает вещи.
     * Выбрасывает ошибку, если не прошло какое-то условие.
     */
    private void checkIsValidUser(Long userId, Long itemId) {
        Item item = itemStorage.getItem(itemId);
        if (item.getOwner() != null && !(item.getOwner().equals(userId))) {
            throw new AuthorizationException("С вещью может работать только её владелец.");
        }
    }
}
