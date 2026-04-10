package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemDto addItem(Long userId, NewItemDto itemDto) {
        log.info("Начался процесс добавления вещи {}", itemDto.getName());
        ItemRequest itemRequest = Optional.ofNullable(itemDto.getRequestId())
                .map(id -> itemRequestRepository.findById(itemDto.getRequestId())
                        .orElseThrow(() -> {
                                    String requestNotFound = "Запрос с id " + itemDto.getRequestId() + "не найден";
                                    log.error(requestNotFound);
                                    return new NotFoundException(requestNotFound);
                                }
                        )
                ).orElse(null);

        if (itemRequest != null) {
            log.info("Запрос {} найден в репозитории для добавления вещи", itemDto.getRequestId());
        }

        Item newItem = ItemMapper.toItem(itemDto, itemRequest);
        log.info("Входной запрос на добавление вещи {} преобразован в соответствующую вещь", itemDto.getName());
        User owner = getValidUserById(userId);
        log.info("Пользователь {} найден в репозитории для добавления вещи", userId);
        newItem.setOwner(owner);
        checkIsValidItem(newItem);
        log.info("Вещь {} прошла валидацию входных данных и готова к добавлению", itemDto.getName());
        return ItemMapper.toItemDto(itemRepository.save(newItem));
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long id, UpdateItemDto itemDto) {
        log.info("Начался процесс обновления вещи {}", itemDto.getName());
        checkIsValidUserForUpdateItem(userId, id);
        log.info("Пользователь {} проверен на возможность работы с вещью при обновлении", userId);
        Item updateItem = ItemMapper.toItem(itemDto);
        log.info("Входной запрос на обновление вещи {} преобразован в соответствующую вещь", id);
        checkIsValidItem(updateItem);
        log.info("Вещь {} прошла валидацию входных данных и готова к обновлению", id);
        Item currentItem = getValidItemById(id);
        updateItemFields(currentItem, updateItem);
        log.info("Вещь {} прошла обновление входных данных и готова к обновлению в БД", id);
        return ItemMapper.toItemDto(itemRepository.save(currentItem));
    }

    @Override
    public ItemDtoGetById getItemById(Long userId, Long id) {
        log.info("Начался процесс получения вещи {} по id", id);
        Item item = getValidItemById(id);
        log.info("Вещь {} найдена в репозитории для возврата по id", userId);
        Booking lastBooking = bookingRepository.findLastApprovedBookingForOwner(id, userId).orElse(null);
        log.info("Последнее бронирование вещи {} найдено в репозитории: {} ", userId, lastBooking);

        Booking nextBooking = bookingRepository.findNextApprovedBookingForOwner(id, userId).orElse(null);
        log.info("Следующее бронирование вещи {} найдено в репозитории: {} ", userId, nextBooking);

        List<Comment> itemComments = commentRepository.findAllByItemId(id);
        log.info("Комментарии для вещи вещи {} найдены в репозитории в количестве: {} ", userId, itemComments.size());
        return ItemMapper.toItemDtoForGetById(item, lastBooking, nextBooking, itemComments);
    }

    @Override
    public List<ItemDtoGet> getUserItems(Long userId) {
        log.info("Начался процесс получения всех вещей пользователя {}, у которых является владельцем", userId);
        List<Item> items = itemRepository.findByOwnerId(userId);
        if (items.isEmpty()) {
            log.info("Для пользователя {} получен пустой список вещей", userId);
            return Collections.emptyList();
        }

        List<Long> itemIds = items.stream().map(Item::getId).toList();
        log.info("Для пользователя {} получен список вещей: ", itemIds);

        Map<Long, List<Comment>> commentsByItemId = commentRepository
                .findAllByItemIdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));
        log.info("Комментарии к вещами пользователя {} сгруппированы по id вещи", itemIds);

        return items.stream()
                .map(item -> ItemMapper.toItemDtoGet(item,
                        commentsByItemId.getOrDefault(item.getId(), Collections.emptyList())))
                .toList();
    }

    @Override
    public List<ItemDto> getAvailableItemsBySearch(String text) {
        log.info("Начался процесс получения доступных вещей, подходящих под запрос: {}", text);
        if (text == null || text.isEmpty()) {
            log.info("Был введён пустой поисковый запрос");
            return Collections.emptyList();
        }
        return itemRepository.getAvailableItemsBySearch(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, NewCommentDto commentDto) {
        log.info("Начался процесс добавления комментарий от пользователя {} к вещи {}: {}",
                userId, itemId, commentDto.getText());
        User author = getValidUserById(userId);
        log.info("Пользователь {} найден в репозитории для добавления комментария", userId);
        Item item = getValidItemById(itemId);

        log.info("Вещь {} найдена в репозитории для добавления комментария", itemId);
        checkIsValidBookingForAddComment(userId, itemId);

        log.info("Бронирование вещи {} найдено и прошло валидацию для добавления комментария", itemId);
        return CommentMapper.toCommentDto(commentRepository.save(CommentMapper.toComment(commentDto, author, item)));
    }

    /**
     * Возвращает вещь по id
     */
    private Item getValidItemById(Long itemId) {
        log.info("Начался внутренний процесс получения вещи {} по id", itemId);
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
        log.info("Начался внутренний процесс получения пользователя {} по id", userId);
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
        log.info("Началась внутренняя проверка вещи {}", item.getId());
        if (item.getOwner() != null) {
            log.info("Началась проверка владельца вещи {}", item.getId());
            getValidUserById(item.getOwner().getId());
            log.info("Проверка владельца вещи {} пройдена", item.getId());
        }

        log.info("Началась проверка имени вещи {}", item.getId());
        if (item.getName() != null && item.getName().isEmpty()) {
            String nameError = "Имя вещи не должно быть пустым";
            log.error(nameError);
            throw new ValidationException(nameError);
        }
        log.info("Проверка имени вещи {} пройдена", item.getId());

        log.info("Началась проверка описания вещи {}", item.getId());
        if (item.getDescription() != null && item.getDescription().isEmpty()) {
            String descriptionError = "Описание вещи не должно быть пустым";
            log.error(descriptionError);
            throw new ValidationException(descriptionError);
        }
        log.info("Проверка описания вещи {} пройдена", item.getId());
        log.info("Закончилась внутренняя проверка вещи {}", item.getId());
    }

    /**
     * Валидация пользователя, который запрашивает вещи для обновления.
     * Выбрасывает ошибку, если не прошло какое-то условие.
     */
    private void checkIsValidUserForUpdateItem(Long userId, Long itemId) {
        log.info("Началась внутренняя проверка пользователя {} для обновления вещи {}", userId, itemId);
        Item item = getValidItemById(itemId);
        log.info("Вещь {} найдена в репозитории для валидации перед обновлением полей", userId);
        if (item.getOwner() != null && !(item.getOwner().getId().equals(userId))) {
            String authorError = "С вещью может работать только её владелец.";
            log.error(authorError);
            throw new AuthorizationException(authorError);
        }
        log.info("Закончилась внутренняя проверка пользователя {} для обновления вещи {}", userId, item.getId());
    }

    /**
     * Валидация бронирования, для вещи которого хотят добавить комментарий.
     * Выбрасывает ошибку, если не прошло какое-то условие.
     */
    private void checkIsValidBookingForAddComment(Long userId, Long itemId) {
        log.info("Началась внутренняя проверка бронирований вещи {} пользователем {}", itemId, userId);
        List<Booking> itemBookings = bookingRepository.findPastByBookerIdAndItemIdAndStatusApproved(userId, itemId);
        log.info("Бронирования вещи {} пользователем {} найдены в количестве: {}", itemId, userId, itemBookings.size());
        if (itemBookings.isEmpty()) {
            throw new ValidationException("Пользователь " + userId + "не бронировал вещь " + itemId);
        }
    }

    /**
     * Обновляет поля вещи из DTO-вещи
     */
    private void updateItemFields(Item currentItem, Item updatedItem) {
        log.info("Началась внутреннее обновление полей вещи {}", currentItem.getId());
        if (updatedItem.getName() != null && !updatedItem.getName().isEmpty()) {
            currentItem.setName(updatedItem.getName());
            log.info("Имя вещи {} обновлено на новое: {}", currentItem.getId(), updatedItem.getName());
        }

        if (updatedItem.getDescription() != null && !updatedItem.getDescription().isEmpty()) {
            currentItem.setDescription(updatedItem.getDescription());
            log.info("Описание вещи {} обновлено на новое: {}", currentItem.getId(), updatedItem.getDescription());
        }

        if (updatedItem.getAvailable() != null) {
            currentItem.setAvailable(updatedItem.getAvailable());
            log.info("Статус доступности вещи {} обновлен: {}", currentItem.getId(), updatedItem.getAvailable());
        }
    }
}
