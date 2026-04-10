package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.AuthorizationException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingDto addBooking(Long userId, NewBookingDto bookingDto) {
        log.info("Началось добавление бронирования для вещи {} от пользователя {}", userId, bookingDto.getItemId());
        Booking booking = getValidBookingForAdd(bookingDto, userId);
        log.info("Входной запрос на добавление бронирования преобразован в соответствующее бронирование");
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto approveBooking(Long userId, Long bookingId, Boolean approved) {
        log.info("Начался процесс изменения статуса бронирования {} на {}", bookingId, approved);

        Booking booking = getValidBookingForApprove(userId, bookingId);
        log.info("Входной запрос на обновление статуса бронирования преобразован в соответствующее бронирование");

        BookingStatus status = approved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        log.info("Итоговый статус бронирования: {}", status);

        booking.setStatus(status);
        log.info("Бронированию {} установлен статус {}", bookingId, status);

        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getBookingById(Long id) {
        log.info("Начался процесс получения бронирования {} по id", id);
        return BookingMapper.toBookingDto(bookingRepository.findById(id)
                .orElseThrow(() -> {
                    String bookingNotFound = "Бронирование с id " + id + " не найдено";
                    log.error(bookingNotFound);
                    return new NotFoundException(bookingNotFound);
                }));
    }

    @Override
    public List<BookingDto> getAllBookingsByUser(Long userId, String stateStr) {
        log.info("Начался процесс получения бронирований, которые сделаны пользователем {}", userId);
        BookingState state = BookingState.valueOf(stateStr);
        log.info("Поиск будет производиться по фильтру {}", state);

        List<Booking> resultList = switch (state) {
            case BookingState.ALL -> bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
            case BookingState.WAITING, BookingState.REJECTED -> bookingRepository
                    .findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.valueOf(stateStr));
            case BookingState.CURRENT -> bookingRepository.findCurrentByBookerId(userId);
            case BookingState.FUTURE -> bookingRepository.findFutureByBookerId(userId);
            case BookingState.PAST -> bookingRepository.findPastByBookerId(userId);
        };
        log.info("Поиск завершён. По букеру найдено бронирований: {}", resultList.size());

        if (!resultList.isEmpty()) {
            return resultList.stream().map(BookingMapper::toBookingDto).toList();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<BookingDto> getAllUserBookings(Long userId, String stateStr) {
        log.info("Начался процесс получения бронирований, у которых владельцем вещи является пользователь {}", userId);
        BookingState state = BookingState.valueOf(stateStr);
        log.info("Поиск будет производиться по состоянию {}", state);

        List<Booking> resultList = switch (state) {
            case BookingState.ALL -> bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId);
            case BookingState.WAITING, BookingState.REJECTED ->
                    bookingRepository.findAllByOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.valueOf(stateStr));
            case BookingState.CURRENT -> bookingRepository.findCurrentByOwnerId(userId);
            case BookingState.FUTURE -> bookingRepository.findFutureByOwnerId(userId);
            case BookingState.PAST -> bookingRepository.findPastByOwnerId(userId);
        };
        log.info("Поиск завершён. По владельцу найдено бронирований: {}", resultList.size());

        if (!resultList.isEmpty()) {
            return resultList.stream().map(BookingMapper::toBookingDto).toList();
        } else {
            String noBooking = "У пользователя " + userId + " нет бронирований";
            log.error(noBooking);
            throw new NotFoundException(noBooking);
        }
    }

    /**
     * Возвращает корректное для добавления бронирование.
     * Если не проходит по условиям, то выбрасывает ошибку
     */
    private Booking getValidBookingForAdd(NewBookingDto bookingDto, Long userId) {
        log.info("Начался внутренний процесс получения валидного бронирования вещи {} перед добавлением в БД",
                bookingDto.getItemId());
        User booker = userRepository.findById(userId).orElseThrow(
                () -> {
                    String bookerNotFound = "Бронирующий с id " + userId + " для бронирования не найден";
                    log.error(bookerNotFound);
                    return new NotFoundException(bookerNotFound);
                });
        log.info("Начался бронирующий с id {}",
                booker.getId());

        if (bookingDto.getStart().isAfter(bookingDto.getEnd())) {
            String notValidBookingDates = "Дата начала бронирования не может быть после даты конца бронирования";
            log.error(notValidBookingDates);
            throw new ValidationException(notValidBookingDates);
        }
        log.info("Даты бронирования вещи {} корректные", bookingDto.getItemId());

        Item item = getValidItem(bookingDto.getItemId());
        return BookingMapper.toBooking(bookingDto, item, booker);
    }

    /**
     * Возвращает корректное для подтверждения бронирование.
     * Если не проходит по условиям, то выбрасывает ошибку
     */
    private Booking getValidBookingForApprove(Long userId, Long bookingId) {
        log.info("Начался внутренний процесс получения валидного бронирования {} перед обновление статуса в БД",
                bookingId);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> {
                    String bookingNotFoundApprove = "Бронирование с id " + bookingId + " для проверки не найдено.";
                    log.error(bookingNotFoundApprove);
                    return new NotFoundException(bookingNotFoundApprove);
                });
        log.info("Бронирование {} найдено в БД", bookingId);

        if (booking.getStatus() != BookingStatus.WAITING) {
            String statusError = "Бронирование уже обработано. Текущий статус: " + booking.getStatus();
            log.error(statusError);
            throw new ValidationException(statusError);
        }
        log.info("У бронирования {} корректный статус для изменения", bookingId);

        if (booking.getItem() == null || booking.getItem().getOwner() == null) {
            String itemError = "Вещь для бронирования с некорректными параметрами";
            log.error(itemError);
            throw new NotFoundException(itemError);
        }
        log.info("У бронирования {} вещь с корректными параметрами", bookingId);

        Long ownerId = booking.getItem().getOwner().getId();
        log.info("У бронирования {} найден владелец с id {}", bookingId, ownerId);
        if (!(ownerId.equals(userId))) {
            String authorError = "С вещью может работать только её владелец.";
            log.error(authorError);
            throw new AuthorizationException(authorError);
        }
        log.info("Пользователь {} может изменять статус бронирования {}", userId, bookingId);
        return booking;
    }

    /**
     * Возвращает корректную для добавления вещь.
     * Если не проходит по условиям, то выбрасывает ошибку
     */
    private Item getValidItem(Long itemId) {
        log.info("Начался внутренний процесс получения валидной вещи для бронирования");
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> {
                    String itemNotFound = "Вещь с id" + itemId + " для бронирования не найдена";
                    log.error(itemNotFound);
                    return new NotFoundException(itemNotFound);
                });
        log.info("Вещь {} найдена в БД", itemId);

        if (!item.getAvailable()) {
            String itemNotAvailable = "Вещь с id" + itemId + " не доступна для бронирования";
            log.error(itemNotAvailable);
            throw new ValidationException(itemNotAvailable);
        }
        log.info("Вещь {} доступна для бронирования", itemId);
        return item;
    }
}