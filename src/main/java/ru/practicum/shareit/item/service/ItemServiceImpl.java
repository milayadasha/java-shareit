package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
        log.info("Вещь {} прошла валидацию входных данных и готова к добавлению", itemDto.getName());
        return ItemMapper.toItemDto(itemStorage.addItem(newItem));
    }

    /**
     * Обновляет вещь
     */
    public ItemDto updateItem(Long userId, Long id, UpdateItemDto itemDto) {
        checkIsValidUser(userId, id);
        log.info("Пользователь {} проверен на возможность работы с вещью", userId);
        Item updateItem = ItemMapper.toItem(itemDto);
        updateItem.setId(id);
        checkIsValidItem(updateItem);
        log.info("Вещь {} прошла валидацию входных данных и готова к обновлению", id);
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
            String ownerError = "Пользователь с id" + item.getOwner() + " для добавления вещи не найден";
            log.error(ownerError);
            throw new NotFoundException(ownerError);
        }

        if (item.getName() != null && item.getName().isEmpty()) {
            String nameError = "Имя вещи не должно быть пустым";
            log.error(nameError);
            throw new ValidationException(nameError);
        }

        if (item.getDescription() != null && item.getDescription().isEmpty()) {
            String descriptionError = "Описание вещи не должно быть пустым";
            log.error(descriptionError);
            throw new ValidationException(descriptionError);
        }
    }

    /**
     * Валидация пользователя, который запрашивает вещи.
     * Выбрасывает ошибку, если не прошло какое-то условие.
     */
    private void checkIsValidUser(Long userId, Long itemId) {
        Item item = itemStorage.getItem(itemId);
        if (item.getOwner() != null && !(item.getOwner().equals(userId))) {
            String authorError = "С вещью может работать только её владелец.";
            log.error(authorError);
            throw new AuthorizationException(authorError);
        }
    }
}
