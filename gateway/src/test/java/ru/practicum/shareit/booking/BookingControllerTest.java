package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {
    @MockBean
    private BookingClient bookingClient;

    @Autowired
    private BookingController bookingController;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private NewBookingDto newBookingDto;
    private BookingDto bookingDto;

    private static final Long BOOKING_ID = 100L;
    private static final Long BOOKER_ID = 1L;
    private static final Long OWNER_ID = 2L;
    private static final Long ITEM_ID = 1L;

    private static final Boolean BOOKING_APPROVED = true;

    private static final String STATUS_WAITING = "WAITING";
    private static final String STATUS_APPROVED = "APPROVED";

    private static final BookingState STATE_ALL = BookingState.ALL;
    private static final BookingState STATE_WAITING = BookingState.WAITING;
    private static final BookingState STATE_CURRENT = BookingState.CURRENT;
    private static final BookingState STATE_FUTURE = BookingState.FUTURE;
    private static final BookingState STATE_PAST = BookingState.PAST;

    @BeforeEach
    void setUp() {
        newBookingDto = new NewBookingDto();
        newBookingDto.setItemId(ITEM_ID);
        newBookingDto.setStart(LocalDateTime.now().plusDays(1));
        newBookingDto.setEnd(LocalDateTime.now().plusDays(2));

        bookingDto = new BookingDto();
        bookingDto.setId(BOOKING_ID);
        bookingDto.setStart(newBookingDto.getStart());
        bookingDto.setEnd(newBookingDto.getEnd());
        bookingDto.setStatus(STATUS_WAITING);
    }

    @Test
    @DisplayName("При успешном добавлении бронирования должен вернуть созданное бронирование")
    void test_addBooking_WhenAddShouldReturnBooking() throws Exception {
        //given && when
        Mockito
                .when(bookingClient.addBooking(BOOKER_ID, newBookingDto))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(bookingDto));

        //then
        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", BOOKER_ID)
                        .content(mapper.writeValueAsString(newBookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(BOOKING_ID))
                .andExpect(jsonPath("$.status").value(STATUS_WAITING));
    }

    @Test
    @DisplayName("При успешном подтверждении бронирования должен вернуть бронирование со статусом APPROVED")
    void test_approveBooking_WhenWaitStatusReturnApproved() throws Exception {
        //given
        bookingDto.setStatus(STATUS_APPROVED);

        //when
        Mockito
                .when(bookingClient.approveBooking(OWNER_ID, BOOKING_ID, BOOKING_APPROVED))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(bookingDto));

        //then
        mvc.perform(patch("/bookings/{bookingId}?approved={approved}", BOOKING_ID, BOOKING_APPROVED)
                        .header("X-Sharer-User-Id", OWNER_ID)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(BOOKING_ID))
                .andExpect(jsonPath("$.status").value(STATUS_APPROVED));
    }

    @Test
    @DisplayName("При запросе бронирования по id должен вернуть бронирование")
    void test_getBookingById_ShouldReturnBooking() throws Exception {
        //given && when
        Mockito
                .when(bookingClient.getBookingById(BOOKING_ID))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(bookingDto));

        //then
        mvc.perform(get("/bookings/{bookingId}", BOOKING_ID)
                        .header("X-Sharer-User-Id", OWNER_ID)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(BOOKING_ID));
    }

    @Test
    @DisplayName("При запросе всех бронирований пользователя со статусом ALL должен вернуть список бронирований")
    void test_getAllBookingsByUser_WhenAllShouldReturnBooking() throws Exception {
        //given && when
        Mockito
                .when(bookingClient.getAllBookingsByUser(BOOKER_ID, STATE_ALL))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(bookingDto)));

        //then
        mvc.perform(get("/bookings?state={state}", STATE_ALL)
                        .header("X-Sharer-User-Id", BOOKER_ID)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].id").value(BOOKING_ID));
    }

    @Test
    @DisplayName("При запросе всех бронирований пользователя со статусом WAITING должен вернуть список бронирований")
    void test_getAllBookingsByUser_WhenWaitingShouldReturnBooking() throws Exception {
        //given && when
        Mockito
                .when(bookingClient.getAllBookingsByUser(BOOKER_ID, STATE_WAITING))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(bookingDto)));

        //then
        mvc.perform(get("/bookings?state={state}", STATE_WAITING)
                        .header("X-Sharer-User-Id", BOOKER_ID)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].id").value(BOOKING_ID))
                .andExpect(jsonPath("[0].status").value(STATUS_WAITING));
    }

    @Test
    @DisplayName("При запросе всех бронирований пользователя со статусом CURRENT должен вернуть список бронирований")
    void test_getAllBookingsByUser_WhenCurrentShouldReturnBooking() throws Exception {
        //given && when
        Mockito
                .when(bookingClient.getAllBookingsByUser(BOOKER_ID, STATE_CURRENT))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(bookingDto)));

        //then
        mvc.perform(get("/bookings?state={state}", STATE_CURRENT)
                        .header("X-Sharer-User-Id", BOOKER_ID)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].id").value(BOOKING_ID));
    }

    @Test
    @DisplayName("При запросе всех бронирований пользователя со статусом FUTURE должен вернуть список бронирований")
    void test_getAllBookingsByUser_WhenFutureShouldReturnBooking() throws Exception {
        //given && when
        Mockito
                .when(bookingClient.getAllBookingsByUser(BOOKER_ID, STATE_FUTURE))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(bookingDto)));

        //then
        mvc.perform(get("/bookings?state={state}", STATE_FUTURE)
                        .header("X-Sharer-User-Id", BOOKER_ID)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].id").value(BOOKING_ID));
    }

    @Test
    @DisplayName("При запросе всех бронирований пользователя со статусом PAST должен вернуть список бронирований")
    void test_getAllBookingsByUser_WhenPastShouldReturnBooking() throws Exception {
        //given && when
        Mockito
                .when(bookingClient.getAllBookingsByUser(BOOKER_ID, STATE_PAST))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(bookingDto)));

        //then
        mvc.perform(get("/bookings?state={state}", STATE_PAST)
                        .header("X-Sharer-User-Id", BOOKER_ID)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].id").value(BOOKING_ID));
    }

    @Test
    @DisplayName("При запросе всех бронирований владельца со статусом ALL должен вернуть список бронирований")
    void test_getAllUserBookings_WhenAllShouldReturnBooking() throws Exception {
        //given && when
        Mockito
                .when(bookingClient.getAllUserBookings(OWNER_ID, STATE_ALL.name()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(bookingDto)));

        //then
        mvc.perform(get("/bookings/owner?state={state}", STATE_ALL)
                        .header("X-Sharer-User-Id", OWNER_ID)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].id").value(BOOKING_ID));
    }

    @Test
    @DisplayName("При запросе всех бронирований владельца со статусом WAITING должен вернуть список бронирований")
    void test_getAllUserBookings_WhenWaitingShouldReturnBooking() throws Exception {
        //given && when
        Mockito
                .when(bookingClient.getAllUserBookings(OWNER_ID, STATE_WAITING.name()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(bookingDto)));

        //then
        mvc.perform(get("/bookings/owner?state={state}", STATE_WAITING)
                        .header("X-Sharer-User-Id", OWNER_ID)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].id").value(BOOKING_ID))
                .andExpect(jsonPath("[0].status").value(STATUS_WAITING));
    }

    @Test
    @DisplayName("При запросе всех бронирований владельца со статусом CURRENT должен вернуть список бронирований")
    void test_getAllUserBookings_WhenCurrentShouldReturnBooking() throws Exception {
        //given && when
        Mockito
                .when(bookingClient.getAllUserBookings(OWNER_ID, STATE_CURRENT.name()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(bookingDto)));

        //then
        mvc.perform(get("/bookings/owner?state={state}", STATE_CURRENT)
                        .header("X-Sharer-User-Id", OWNER_ID)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].id").value(BOOKING_ID));
    }

    @Test
    @DisplayName("При запросе всех бронирований владельца со статусом FUTURE должен вернуть список бронирований")
    void test_getAllUserBookings_WhenFutureShouldReturnBooking() throws Exception {
        //given && when
        Mockito
                .when(bookingClient.getAllUserBookings(OWNER_ID, STATE_FUTURE.name()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(bookingDto)));

        //then
        mvc.perform(get("/bookings/owner?state={state}", STATE_FUTURE)
                        .header("X-Sharer-User-Id", OWNER_ID)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].id").value(BOOKING_ID));
    }

    @Test
    @DisplayName("При запросе всех бронирований владельца со статусом PAST должен вернуть список бронирований")
    void test_getAllUserBookings_WhenPastShouldReturnBooking() throws Exception {
        //given && when
        Mockito
                .when(bookingClient.getAllUserBookings(OWNER_ID, STATE_PAST.name()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(bookingDto)));

        //then
        mvc.perform(get("/bookings/owner?state={state}", STATE_PAST)
                        .header("X-Sharer-User-Id", OWNER_ID)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].id").value(BOOKING_ID));
    }
}
