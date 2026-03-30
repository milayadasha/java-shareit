package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.AuthorizationException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.CommentMapper;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private CommentRepository commentRepository;

    /**
     * Добавляет новую вещь
     */
    @Override
    @Transactional
    public ItemDto addItem(Long userId, NewItemDto itemDto) {
        Item newItem = ItemMapper.toItem(itemDto);
        User owner = getValidUserById(userId);
        newItem.setOwner(owner);
        checkIsValidItem(newItem);
        log.info("Вещь {} прошла валидацию входных данных и готова к добавлению", itemDto.getName());
        return ItemMapper.toItemDto(itemRepository.save(newItem));
    }

    /**
     * Обновляет вещь
     */
    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long id, UpdateItemDto itemDto) {
        checkIsValidUserForUpdateItem(userId, id);
        log.info("Пользователь {} проверен на возможность работы с вещью", userId);
        Item updateItem = ItemMapper.toItem(itemDto);
        checkIsValidItem(updateItem);
        Item currentItem = getValidItemById(id);
        updateItemFields(currentItem, updateItem);
        log.info("Вещь {} прошла валидацию входных данных и готова к обновлению", id);
        return ItemMapper.toItemDto(itemRepository.save(currentItem));
    }

    /**
     * Возвращает вещь по id
     */
    @Override
    public ItemDtoGetById getItemById(Long userId, Long id) {
        Item item = getValidItemById(id);
        Booking lastBooking = bookingRepository.findLastApprovedBookingForOwner(id, userId).orElse(null);
        Booking nextBooking = bookingRepository.findNextApprovedBookingForOwner(id, userId).orElse(null);
        List<Comment> itemComments = commentRepository.findAllByItemId(id);
        return ItemMapper.toItemDtoForGetById(item, lastBooking, nextBooking, itemComments);
    }

    /**
     * Возвращает все вещи пользователя, у которых он является владельцем
     */
    @Override
    public List<ItemDtoGet> getUserItems(Long userId) {
        List<Item> items = itemRepository.findByOwnerId(userId);
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> itemIds = items.stream().map(Item::getId).toList();
        Map<Long, List<Comment>> commentsByItemId = commentRepository
                .findAllByItemIdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));

        return items.stream()
                .map(item -> ItemMapper.toItemDtoGet(item,
                        commentsByItemId.getOrDefault(item.getId(), Collections.emptyList())))
                .toList();
    }

    /**
     * Возвращает все вещи, которые содержат поисковую строку в названии или описании.
     * Вещи доступны для бронирования.
     */
    @Override
    public List<ItemDto> getAvailableItemsBySearch(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        return itemRepository.getAvailableItemsBySearch(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    /**
     * Добавляет комментарий
     */
    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, NewCommentDto commentDto) {
        User author = getValidUserById(userId);
        Item item = getValidItemById(itemId);
        checkIsValidBookingForAddComment(userId, itemId);
        log.info("Пользователь {} и вещь {} найдены для добавления комментария", userId, itemId);
        return CommentMapper.toCommentDto(commentRepository.save(CommentMapper.toComment(commentDto, author, item)));
    }

    /**
     * Возвращает вещь по id
     */
    private Item getValidItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    String itemNotFound = "Вещь с id " + itemId + " не найдена.";
                    log.error(itemNotFound);
                    return new NotFoundException(itemNotFound);
                });
    }

    /**
     * Возвращает пользователя по id
     */
    private User getValidUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    String userNotFound = "Пользователь с id " + userId +
                            "не найден";
                    log.error(userNotFound);
                    return new NotFoundException(userNotFound);
                });
    }

    /**
     * Валидация вещи.
     * Выбрасывает ошибку, если не прошло какое-то условие.
     */
    private void checkIsValidItem(Item item) {
        if (item.getOwner() != null) {
            getValidUserById(item.getOwner().getId());
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
     * Валидация пользователя, который запрашивает вещи для обновления.
     * Выбрасывает ошибку, если не прошло какое-то условие.
     */
    private void checkIsValidUserForUpdateItem(Long userId, Long itemId) {
        Item item = getValidItemById(itemId);
        if (item.getOwner() != null && !(item.getOwner().getId().equals(userId))) {
            String authorError = "С вещью может работать только её владелец.";
            log.error(authorError);
            throw new AuthorizationException(authorError);
        }
    }

    /**
     * Валидация бронирования, для вещи которого хотят добавить комментарий.
     * Выбрасывает ошибку, если не прошло какое-то условие.
     */
    private void checkIsValidBookingForAddComment(Long userId, Long itemId) {
        List<Booking> itemBookings = bookingRepository.findPastByBookerIdAndItemIdAndStatusApproved(userId, itemId);
        if (itemBookings.isEmpty()) {
            throw new ValidationException("Пользователь " + userId + "не бронировал вещь " + itemId);
        }
    }

    /**
     * Обновляет поля вещи из DTO-вещи
     */
    private void updateItemFields(Item currentItem, Item updatedItem) {
        if (updatedItem.getName() != null && !updatedItem.getName().isEmpty()) {
            currentItem.setName(updatedItem.getName());
        }

        if (updatedItem.getDescription() != null && !updatedItem.getDescription().isEmpty()) {
            currentItem.setDescription(updatedItem.getDescription());
        }

        if (updatedItem.getAvailable() != null) {
            currentItem.setAvailable(updatedItem.getAvailable());
        }
    }
}
