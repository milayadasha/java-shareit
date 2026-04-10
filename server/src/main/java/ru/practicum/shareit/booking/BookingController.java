package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.service.BookingServiceImpl;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    @Autowired
    private BookingServiceImpl bookingService;

    /**
     * Добавляет новое бронирование.
     */
    @PostMapping
    public ResponseEntity<BookingDto> addBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @RequestBody NewBookingDto newBookingDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.addBooking(userId, newBookingDto));
    }

    /**
     * Обновляет статус бронирования.
     */
    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> approveBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @PathVariable long bookingId,
                                                     @RequestParam Boolean approved) {
        return ResponseEntity.ok(bookingService.approveBooking(userId, bookingId, approved));
    }

    /**
     * Возвращает бронирование по id
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable long bookingId) {
        return ResponseEntity.ok(bookingService.getBookingById(bookingId));
    }

    /**
     * Возвращает бронирования, сделанные пользователем
     */
    @GetMapping
    public ResponseEntity<List<BookingDto>> getAllBookingsByUser(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                                 @RequestParam(required = false, defaultValue = "ALL")
                                                                 String state) {
        return ResponseEntity.ok(bookingService.getAllBookingsByUser(userId, state));
    }

    /**
     * Возвращает бронирования, у которых пользователь является хозяином вещей
     */
    @GetMapping("/owner")
    public ResponseEntity<List<BookingDto>> getAllUserBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                               @RequestParam(required = false, defaultValue = "ALL")
                                                               String state) {
        return ResponseEntity.ok(bookingService.getAllUserBookings(userId, state));
    }
}
