package ru.practicum.shareit.booking;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.dto.NewBookingDto;


@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    /**
     * Добавляет новое бронирование.
     */
    @PostMapping
    public ResponseEntity<Object> addBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @RequestBody @Valid NewBookingDto requestDto) {
        log.info("Добавление бронирование {} от пользователя {}", requestDto, userId);
        return bookingClient.addBooking(userId, requestDto);
    }

    /**
     * Обновляет статус бронирования.
     */
    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                                 @PathVariable long bookingId,
                                                 @RequestParam Boolean approved) {
        log.info("Проставляем статус бронирования {} для пользователя {}", bookingId, userId);
        return bookingClient.approveBooking(userId, bookingId, approved);
    }

    /**
     * Возвращает бронирование по id
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@PathVariable Long bookingId) {
        log.info("Получаем бронирование {} ", bookingId);
        return bookingClient.getBookingById(bookingId);
    }

    /**
     * Возвращает бронирования, сделанные пользователем
     */
    @GetMapping
    public ResponseEntity<Object> getAllBookingsByUser(@RequestHeader("X-Sharer-User-Id") long userId,
                                                       @RequestParam(name = "state", defaultValue = "ALL")
                                                       String stateParam) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Неизвестное состояние: " + stateParam));
        log.info("Получаем бронирование с состоянием {} от пользователя {}", stateParam, userId);
        return bookingClient.getAllBookingsByUser(userId, state);
    }

    /**
     * Возвращает бронирования, у которых пользователь является хозяином вещей
     */
    @GetMapping("/owner")
    public ResponseEntity<Object> getAllUserBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                                     @RequestParam(name = "state", defaultValue = "ALL")
                                                     String stateParam) {
        log.info("Получаем бронирование с состоянием {} для пользователя {}", stateParam, userId);
        return bookingClient.getAllUserBookings(userId, stateParam);
    }
}