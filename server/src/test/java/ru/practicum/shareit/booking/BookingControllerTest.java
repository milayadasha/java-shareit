package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.user.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {
    @Mock
    private BookingServiceImpl bookingService;

    @InjectMocks
    private BookingController bookingController;

    private MockMvc mvc;

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    private NewBookingDto newBookingDto;
    private BookingDto bookingDto;
    private User booker;
    private User owner;

    private static final Long BOOKING_ID = 100L;
    private static final Long BOOKER_ID = 1L;
    private static final Long OWNER_ID = 2L;

    private static final Boolean BOOKING_APPROVED = true;

    private static final BookingStatus STATUS_WAITING = BookingStatus.WAITING;
    private static final BookingStatus STATUS_APPROVED = BookingStatus.APPROVED;

    private static final String STATE_ALL = "ALL";
    private static final String STATE_WAITING = "WAITING";
    private static final String STATE_CURRENT = "CURRENT";
    private static final String STATE_FUTURE = "FUTURE";
    private static final String STATE_PAST = "PAST";

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(bookingController).build();

        newBookingDto = new NewBookingDto();
        newBookingDto.setStart(LocalDateTime.now().plusDays(1));
        newBookingDto.setEnd(LocalDateTime.now().plusDays(2));

        bookingDto = new BookingDto();
        bookingDto.setId(BOOKING_ID);
        bookingDto.setStart(newBookingDto.getStart());
        bookingDto.setEnd(newBookingDto.getEnd());
        bookingDto.setStatus(STATUS_WAITING.toString());

        booker = new User();
        booker.setId(BOOKER_ID);

        owner = new User();
        owner.setId(OWNER_ID);
    }

    @Test
    @DisplayName("При добавлении бронирования должен сохранить и вернуть бронирование")
    void test_addBooking_WhenAddShouldReturnBooking() throws Exception {
        //given && when
        Mockito
                .when(bookingService.addBooking(booker.getId(), newBookingDto))
                .thenReturn(bookingDto);
        //then
        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", booker.getId())
                        .content(mapper.writeValueAsString(newBookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(BOOKING_ID))
                .andExpect(jsonPath("$.status").value(STATUS_WAITING.toString()));
    }

    @Test
    @DisplayName("При подтверждении бронирования должен изменить статус на APPROVED")
    void test_approveBooking_WhenWaitStatusReturnApproved() throws Exception {
        //given
        bookingDto.setStatus(STATUS_APPROVED.toString());

        //when
        Mockito
                .when(bookingService.approveBooking(OWNER_ID, BOOKING_ID, BOOKING_APPROVED))
                .thenReturn(bookingDto);

        //then
        mvc.perform(patch("/bookings/{bookingId}?approved={approved}", BOOKING_ID, BOOKING_APPROVED)
                        .header("X-Sharer-User-Id", owner.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(BOOKING_ID))
                .andExpect(jsonPath("$.status").value(STATUS_APPROVED.toString()));
    }

    @Test
    void test_getBookingById_ShouldReturnBooking() throws Exception {
        //given && when
        Mockito
                .when(bookingService.getBookingById(BOOKING_ID))
                .thenReturn(bookingDto);

        //then
        mvc.perform(get("/bookings/{bookingId}", BOOKING_ID)
                        .header("X-Sharer-User-Id", owner.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(BOOKING_ID));
    }

    @Test
    @DisplayName("При запросе всех бронирований пользователя со статусом ALL должен вернуть список")
    void test_getAllBookingsByUser_WhenAllShouldReturnBooking() throws Exception {
        //given && when
        Mockito
                .when(bookingService.getAllBookingsByUser(BOOKER_ID, STATE_ALL))
                .thenReturn(List.of(bookingDto));

        //then
        mvc.perform(get("/bookings?state={state}", STATE_ALL)
                        .header("X-Sharer-User-Id", booker.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].id").value(BOOKING_ID));
    }

    @Test
    @DisplayName("При запросе всех бронирований пользователя со статусом WAITING должен вернуть список")
    void test_getAllBookingsByUser_WhenWaitingShouldReturnBooking() throws Exception {
        //given && when
        Mockito
                .when(bookingService.getAllBookingsByUser(BOOKER_ID, STATE_WAITING))
                .thenReturn(List.of(bookingDto));

        //then
        mvc.perform(get("/bookings?state={state}", STATE_WAITING)
                        .header("X-Sharer-User-Id", booker.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].id").value(BOOKING_ID))
                .andExpect(jsonPath("[0].status").value(STATUS_WAITING.toString()));
    }

    @Test
    @DisplayName("При запросе всех бронирований пользователя со статусом CURRENT должен вернуть список")
    void test_getAllBookingsByUser_WhenCurrentShouldReturnBooking() throws Exception {
        //given && when
        Mockito
                .when(bookingService.getAllBookingsByUser(BOOKER_ID, STATE_CURRENT))
                .thenReturn(List.of(bookingDto));

        //then
        mvc.perform(get("/bookings?state={state}", STATE_CURRENT)
                        .header("X-Sharer-User-Id", booker.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].id").value(BOOKING_ID));
    }

    @Test
    @DisplayName("При запросе всех бронирований пользователя со статусом FUTURE должен вернуть список")
    void test_getAllBookingsByUser_WhenFutureShouldReturnBooking() throws Exception {
        //given && when
        Mockito
                .when(bookingService.getAllBookingsByUser(BOOKER_ID, STATE_FUTURE))
                .thenReturn(List.of(bookingDto));

        //then
        mvc.perform(get("/bookings?state={state}", STATE_FUTURE)
                        .header("X-Sharer-User-Id", booker.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].id").value(BOOKING_ID));
    }

    @Test
    @DisplayName("При запросе всех бронирований пользователя со статусом PAST должен вернуть список")
    void test_getAllBookingsByUser_WhenPastShouldReturnBooking() throws Exception {
        //given && when
        Mockito
                .when(bookingService.getAllBookingsByUser(BOOKER_ID, STATE_PAST))
                .thenReturn(List.of(bookingDto));

        //then
        mvc.perform(get("/bookings?state={state}", STATE_PAST)
                        .header("X-Sharer-User-Id", booker.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].id").value(BOOKING_ID));
    }

    @Test
    @DisplayName("При запросе всех бронирований владельца со статусом ALL должен вернуть список")
    void test_getAllUserBookings_WhenAllShouldReturnBooking() throws Exception {
        //given && when
        Mockito
                .when(bookingService.getAllUserBookings(OWNER_ID, STATE_ALL))
                .thenReturn(List.of(bookingDto));

        //then
        mvc.perform(get("/bookings/owner?state={state}", STATE_ALL)
                        .header("X-Sharer-User-Id", owner.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].id").value(BOOKING_ID));
    }

    @Test
    @DisplayName("При запросе всех бронирований владельца со статусом WAITING должен вернуть список")
    void test_getAllUserBookings_WhenWaitingShouldReturnBooking() throws Exception {
        //given && when
        Mockito
                .when(bookingService.getAllUserBookings(OWNER_ID, STATE_WAITING))
                .thenReturn(List.of(bookingDto));

        //then
        mvc.perform(get("/bookings/owner?state={state}", STATE_WAITING)
                        .header("X-Sharer-User-Id", owner.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].id").value(BOOKING_ID))
                .andExpect(jsonPath("[0].status").value(STATUS_WAITING.toString()));
    }

    @Test
    @DisplayName("При запросе всех бронирований владельца со статусом CURRENT должен вернуть список")
    void test_getAllUserBookings_WhenCurrentShouldReturnBooking() throws Exception {
        //given && when
        Mockito
                .when(bookingService.getAllUserBookings(OWNER_ID, STATE_CURRENT))
                .thenReturn(List.of(bookingDto));

        //then
        mvc.perform(get("/bookings/owner?state={state}", STATE_CURRENT)
                        .header("X-Sharer-User-Id", owner.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].id").value(BOOKING_ID));
    }

    @Test
    @DisplayName("При запросе всех бронирований владельца со статусом FUTURE должен вернуть список")
    void test_getAllUserBookings_WhenFutureShouldReturnBooking() throws Exception {
        //given && when
        Mockito
                .when(bookingService.getAllUserBookings(OWNER_ID, STATE_FUTURE))
                .thenReturn(List.of(bookingDto));

        //then
        mvc.perform(get("/bookings/owner?state={state}", STATE_FUTURE)
                        .header("X-Sharer-User-Id", owner.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].id").value(BOOKING_ID));
    }

    @Test
    @DisplayName("При запросе всех бронирований владельца со статусом PAST должен вернуть список")
    void test_getAllUserBookings_WhenPastShouldReturnBooking() throws Exception {
        //given && when
        Mockito
                .when(bookingService.getAllUserBookings(OWNER_ID, STATE_PAST))
                .thenReturn(List.of(bookingDto));

        //then
        mvc.perform(get("/bookings/owner?state={state}", STATE_PAST)
                        .header("X-Sharer-User-Id", owner.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].id").value(BOOKING_ID));
    }
}