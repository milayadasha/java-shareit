package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;

import java.util.List;

public interface BookingService {
    /**
     * Добавляет новое бронирование
     */
    BookingDto addBooking(Long userId, NewBookingDto bookingDto);

    /**
     * Подтверждает или отклоняет бронирование
     */
    BookingDto approveBooking(Long userId, Long bookingId, Boolean approved);

    /**
     * Возвращает бронирование по id
     */
    BookingDto getBookingById(Long id);

    /**
     * Возвращает все бронирования, сделанные пользователем
     */
    List<BookingDto> getAllBookingsByUser(Long userId, String stateStr);

    /**
     * Возвращает все бронирования, у которых пользователь является владельцем
     */
    List<BookingDto> getAllUserBookings(Long userId, String stateStr);
}
