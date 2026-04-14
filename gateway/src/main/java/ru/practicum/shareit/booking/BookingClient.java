package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.client.BaseClient;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    /**
     * Добавляет новое бронирование.
     */
    public ResponseEntity<Object> addBooking(long userId, NewBookingDto requestDto) {
        return post("", userId, requestDto);
    }

    /**
     * Обновляет статус бронирования.
     */
    public ResponseEntity<Object> getBookingById(long bookingId) {
        return get("/" + bookingId);
    }

    /**
     * Возвращает бронирование по id
     */
    public ResponseEntity<Object> approveBooking(long userId, Long bookingId, Boolean approved) {
        return patch("/" + bookingId + "?approved=" + approved, userId);
    }

    /**
     * Возвращает бронирования, сделанные пользователем
     */
    public ResponseEntity<Object> getAllBookingsByUser(long userId, BookingState state) {
        return get("?state=" + state, userId);
    }

    /**
     * Возвращает бронирования, у которых пользователь является хозяином вещей
     */
    public ResponseEntity<Object> getAllUserBookings(Long userId, String state) {
        return get("/owner?state=" + state, userId);
    }
}