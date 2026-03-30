package ru.practicum.shareit.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class BookingServiceImpl implements BookingService {
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ItemRepository itemRepository;

    /**
     * Добавляет новое бронирование
     */
    @Override
    @Transactional
    public BookingDto addBooking(Long userId, NewBookingDto bookingDto) {
        Booking booking = getValidBookingForAdd(bookingDto, userId);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    /**
     * Подтверждает или отклоняет бронирование
     */
    @Override
    @Transactional
    public BookingDto approveBooking(Long userId, Long bookingId, Boolean approved) {
        Booking booking = getValidBookingForApprove(userId, bookingId);
        BookingStatus status = approved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        booking.setStatus(status);
        log.info("Бронированию {} установлен статус {}", bookingId, status);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    /**
     * Возвращает бронирование по id
     */
    @Override
    public BookingDto getBookingById(Long id) {
        return BookingMapper.toBookingDto(bookingRepository.findById(id)
                .orElseThrow(() -> {
                    String bookingNotFound = "Бронирование с id " + id + " не найдено";
                    log.error(bookingNotFound);
                    return new NotFoundException(bookingNotFound);
                }));
    }

    /**
     * Возвращает все бронирования, сделанные пользователем
     */
    @Override
    public List<BookingDto> getAllBookingsByUser(Long userId, String stateStr) {
        BookingState state = BookingState.valueOf(stateStr);
        List<Booking> resultList = switch (state) {
            case BookingState.ALL -> bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
            case BookingState.WAITING, BookingState.REJECTED -> bookingRepository
                    .findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.valueOf(stateStr));
            case BookingState.CURRENT -> bookingRepository.findCurrentByBookerId(userId);
            case BookingState.FUTURE -> bookingRepository.findFutureByBookerId(userId);
            case BookingState.PAST -> bookingRepository.findPastByBookerId(userId);
        };

        if (!resultList.isEmpty()) {
            return resultList.stream().map(BookingMapper::toBookingDto).toList();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Возвращает все бронирования, у которых пользователь является владельцем
     */
    @Override
    public List<BookingDto> getAllUserBookings(Long userId, String stateStr) {
        BookingState state = BookingState.valueOf(stateStr);
        List<Booking> resultList = switch (state) {
            case BookingState.ALL -> bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId);
            case BookingState.WAITING, BookingState.REJECTED ->
                    bookingRepository.findAllByOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.valueOf(stateStr));
            case BookingState.CURRENT -> bookingRepository.findCurrentByOwnerId(userId);
            case BookingState.FUTURE -> bookingRepository.findFutureByOwnerId(userId);
            case BookingState.PAST -> bookingRepository.findPastByOwnerId(userId);
        };

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
        User booker = userRepository.findById(userId).orElseThrow(
                () -> {
                    String bookerNotFound = "Бронирующий с id " + userId + " для бронирования не найден";
                    log.error(bookerNotFound);
                    return new NotFoundException(bookerNotFound);
                });

        if (bookingDto.getStart().isAfter(bookingDto.getEnd())) {
            String notValidBookingDates = "Дата начала бронирования не может быть после даты конца бронирования";
            log.error(notValidBookingDates);
            throw new ValidationException(notValidBookingDates);
        }

        Item item = getValidItem(bookingDto.getItemId());
        return BookingMapper.toBooking(bookingDto, item, booker);
    }

    /**
     * Возвращает корректное для подтверждения бронирование.
     * Если не проходит по условиям, то выбрасывает ошибку
     */
    private Booking getValidBookingForApprove(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> {
                    String bookingNotFoundApprove = "Бронирование с id " + bookingId + " для проверки не найдено.";
                    log.error(bookingNotFoundApprove);
                    return new NotFoundException(bookingNotFoundApprove);
                });

        if (booking.getStatus() != BookingStatus.WAITING) {
            String statusError = "Бронирование уже обработано. Текущий статус: " + booking.getStatus();
            log.error(statusError);
            throw new ValidationException(statusError);
        }

        if (booking.getItem() == null || booking.getItem().getOwner() == null) {
            String itemError = "Вещь для бронирования с некорректными параметрами";
            log.error(itemError);
            throw new NotFoundException(itemError);
        }

        Long ownerId = booking.getItem().getOwner().getId();
        if (!(ownerId.equals(userId))) {
            String authorError = "С вещью может работать только её владелец.";
            log.error(authorError);
            throw new AuthorizationException(authorError);
        }
        return booking;
    }

    /**
     * Возвращает корректную для добавления вещь.
     * Если не проходит по условиям, то выбрасывает ошибку
     */
    private Item getValidItem(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> {
                    String itemNotFound = "Вещь с id" + itemId + " для бронирования не найдена";
                    log.error(itemNotFound);
                    return new NotFoundException(itemNotFound);
                });

        if (!item.getAvailable()) {
            String itemNotAvailable = "Вещь с id" + itemId + " не доступна для бронирования";
            log.error(itemNotAvailable);
            throw new ValidationException(itemNotAvailable);
        }
        return item;
    }
}
