package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplUnitTest {
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User booker;
    private Item item;
    private Booking booking;

    private static final Long BOOKING_ID = 100L;
    private static final Long BOOKER_ID = 1L;
    private static final Long OWNER_ID = 2L;
    private static final Long ITEM_ID = 3L;

    private static final Boolean ITEM_AVAILABLE = true;
    private static final Boolean BOOKING_APPROVED = true;

    private static final BookingStatus STATUS_WAITING = BookingStatus.WAITING;
    private static final BookingStatus STATUS_APPROVED = BookingStatus.APPROVED;

    private static final String STATE_ALL = "ALL";
    private static final String STATE_WAITING = "WAITING";
    private static final String STATE_CURRENT = "CURRENT";
    private static final String STATE_FUTURE = "FUTURE";
    private static final String STATE_PAST = "PAST";

    @BeforeEach
    void setUpData() {
        booker = new User();
        booker.setId(BOOKER_ID);

        item = new Item();
        item.setId(ITEM_ID);
        item.setAvailable(ITEM_AVAILABLE);

        booking = new Booking();
        booking.setId(BOOKING_ID);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(STATUS_WAITING);
    }

    @Test
    @DisplayName("При добавлении бронирования должен сохранить и вернуть бронирование")
    void test_addBooking_WhenAddShouldReturnBooking() {
        //given
        NewBookingDto dto = new NewBookingDto();
        dto.setItemId(ITEM_ID);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));

        Mockito
                .when(userRepository.findById(BOOKER_ID))
                .thenReturn(Optional.of(booker));
        Mockito
                .when(itemRepository.findById(ITEM_ID))
                .thenReturn(Optional.of(item));

        Mockito.
                when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        // when
        BookingDto result = bookingService.addBooking(BOOKER_ID, dto);

        // then
        assertThat(result.getId()).isEqualTo(BOOKING_ID);
        assertThat(result.getStatus()).isEqualTo(String.valueOf(STATUS_WAITING));

        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(userRepository, times(1)).findById(BOOKER_ID);
        verify(itemRepository, times(1)).findById(ITEM_ID);
    }

    @Test
    @DisplayName("При подтверждении бронирования должен изменить статус на APPROVED")
    void test_approveBooking_WhenWaitStatusReturnApproved() {
        //given
        User owner = new User();
        owner.setId(OWNER_ID);
        item.setOwner(owner);

        Mockito.
                when(bookingRepository.findById(BOOKING_ID))
                .thenReturn(Optional.of(booking));
        Mockito.
                when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        //when
        BookingDto result = bookingService.approveBooking(OWNER_ID, BOOKING_ID, BOOKING_APPROVED);

        //then
        assertThat(result.getId()).isEqualTo(BOOKING_ID);
        assertThat(result.getStatus()).isEqualTo(String.valueOf(STATUS_APPROVED));

        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(bookingRepository, times(1)).findById(BOOKING_ID);
    }

    @Test
    void test_getBookingById_ShouldReturnBooking() {
        //given && when
        Mockito.when(bookingRepository.findById(BOOKING_ID))
                .thenReturn(Optional.of(booking));

        // then
        BookingDto result = bookingService.getBookingById(BOOKING_ID);

        assertThat(result.getId()).isEqualTo(BOOKING_ID);
        assertThat(result.getStatus()).isEqualTo(String.valueOf(STATUS_WAITING));
        assertThat(result.getBooker().getId()).isEqualTo(BOOKER_ID);
        assertThat(result.getItem().getId()).isEqualTo(ITEM_ID);
        assertThat(result.getItem().getAvailable()).isEqualTo(ITEM_AVAILABLE);

        verify(bookingRepository, times(1)).findById(BOOKING_ID);
    }

    @Test
    @DisplayName("При запросе всех бронирований пользователя со статусом ALL должен вернуть список")
    void test_getAllBookingsByUser_WhenAllShouldReturnBooking() {
        //given
        Mockito.
                when(bookingRepository.findAllByBookerIdOrderByStartDesc(BOOKER_ID))
                .thenReturn(List.of(booking));

        //when
        List<BookingDto> result = bookingService.getAllBookingsByUser(BOOKER_ID, STATE_ALL);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getStatus()).isEqualTo(String.valueOf(STATUS_WAITING));
        assertThat(result.get(0).getBooker().getId()).isEqualTo(BOOKER_ID);
        assertThat(result.get(0).getItem().getId()).isEqualTo(ITEM_ID);
        assertThat(result.get(0).getItem().getAvailable()).isEqualTo(ITEM_AVAILABLE);

        verify(bookingRepository, times(1)).findAllByBookerIdOrderByStartDesc(BOOKER_ID);
    }

    @Test
    @DisplayName("При запросе всех бронирований пользователя со статусом WAITING должен вернуть список")
    void test_getAllBookingsByUser_WhenWaitingShouldReturnBooking() {
        //given
        Mockito.
                when(bookingRepository
                        .findAllByBookerIdAndStatusOrderByStartDesc(BOOKER_ID, BookingStatus.valueOf(STATE_WAITING)))
                .thenReturn(List.of(booking));

        //when
        List<BookingDto> result = bookingService.getAllBookingsByUser(BOOKER_ID, STATE_WAITING);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getStatus()).isEqualTo(String.valueOf(STATUS_WAITING));
        assertThat(result.get(0).getBooker().getId()).isEqualTo(BOOKER_ID);
        assertThat(result.get(0).getItem().getId()).isEqualTo(ITEM_ID);
        assertThat(result.get(0).getItem().getAvailable()).isEqualTo(ITEM_AVAILABLE);

        verify(bookingRepository, times(1))
                .findAllByBookerIdAndStatusOrderByStartDesc(BOOKER_ID, BookingStatus.valueOf(STATE_WAITING));
    }

    @Test
    @DisplayName("При запросе всех бронирований пользователя со статусом CURRENT должен вернуть список")
    void test_getAllBookingsByUser_WhenCurrentShouldReturnBooking() {
        //given
        Mockito.
                when(bookingRepository.findCurrentByBookerId(BOOKER_ID))
                .thenReturn(List.of(booking));

        //when
        List<BookingDto> result = bookingService.getAllBookingsByUser(BOOKER_ID, STATE_CURRENT);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getStatus()).isEqualTo(String.valueOf(STATUS_WAITING));
        assertThat(result.get(0).getBooker().getId()).isEqualTo(BOOKER_ID);
        assertThat(result.get(0).getItem().getId()).isEqualTo(ITEM_ID);
        assertThat(result.get(0).getItem().getAvailable()).isEqualTo(ITEM_AVAILABLE);

        verify(bookingRepository, times(1)).findCurrentByBookerId(BOOKER_ID);
    }

    @Test
    @DisplayName("При запросе всех бронирований пользователя со статусом FUTURE должен вернуть список")
    void test_getAllBookingsByUser_WhenFutureShouldReturnBooking() {
        //given
        Mockito.
                when(bookingRepository.findFutureByBookerId(BOOKER_ID))
                .thenReturn(List.of(booking));

        //when
        List<BookingDto> result = bookingService.getAllBookingsByUser(BOOKER_ID, STATE_FUTURE);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getStatus()).isEqualTo(String.valueOf(STATUS_WAITING));
        assertThat(result.get(0).getBooker().getId()).isEqualTo(BOOKER_ID);
        assertThat(result.get(0).getItem().getId()).isEqualTo(ITEM_ID);
        assertThat(result.get(0).getItem().getAvailable()).isEqualTo(ITEM_AVAILABLE);

        verify(bookingRepository, times(1)).findFutureByBookerId(BOOKER_ID);
    }

    @Test
    @DisplayName("При запросе всех бронирований пользователя со статусом PAST должен вернуть список")
    void test_getAllBookingsByUser_WhenPastShouldReturnBooking() {
        //given
        Mockito.
                when(bookingRepository.findPastByBookerId(BOOKER_ID))
                .thenReturn(List.of(booking));

        //when
        List<BookingDto> result = bookingService.getAllBookingsByUser(BOOKER_ID, STATE_PAST);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getStatus()).isEqualTo(String.valueOf(STATUS_WAITING));
        assertThat(result.get(0).getBooker().getId()).isEqualTo(BOOKER_ID);
        assertThat(result.get(0).getItem().getId()).isEqualTo(ITEM_ID);
        assertThat(result.get(0).getItem().getAvailable()).isEqualTo(ITEM_AVAILABLE);

        verify(bookingRepository, times(1)).findPastByBookerId(BOOKER_ID);
    }

    @Test
    @DisplayName("При запросе всех бронирований владельца со статусом ALL должен вернуть список")
    void test_getAllUserBookings_WhenAllShouldReturnBooking() {
        //given
        Mockito.
                when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(OWNER_ID))
                .thenReturn(List.of(booking));

        //when
        List<BookingDto> result = bookingService.getAllUserBookings(OWNER_ID, STATE_ALL);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getStatus()).isEqualTo(String.valueOf(STATUS_WAITING));
        assertThat(result.get(0).getBooker().getId()).isEqualTo(BOOKER_ID);
        assertThat(result.get(0).getItem().getId()).isEqualTo(ITEM_ID);
        assertThat(result.get(0).getItem().getAvailable()).isEqualTo(ITEM_AVAILABLE);

        verify(bookingRepository, times(1)).findAllByItemOwnerIdOrderByStartDesc(OWNER_ID);
    }

    @Test
    @DisplayName("При запросе всех бронирований владельца со статусом WAITING должен вернуть список")
    void test_getAllUserBookings_WhenWaitingShouldReturnBooking() {
        //given
        Mockito.
                when(bookingRepository
                        .findAllByOwnerIdAndStatusOrderByStartDesc(OWNER_ID, BookingStatus.valueOf(STATE_WAITING)))
                .thenReturn(List.of(booking));

        //when
        List<BookingDto> result = bookingService.getAllUserBookings(OWNER_ID, STATE_WAITING);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getStatus()).isEqualTo(String.valueOf(STATUS_WAITING));
        assertThat(result.get(0).getBooker().getId()).isEqualTo(BOOKER_ID);
        assertThat(result.get(0).getItem().getId()).isEqualTo(ITEM_ID);
        assertThat(result.get(0).getItem().getAvailable()).isEqualTo(ITEM_AVAILABLE);

        verify(bookingRepository, times(1))
                .findAllByOwnerIdAndStatusOrderByStartDesc(OWNER_ID, BookingStatus.valueOf(STATE_WAITING));
    }

    @Test
    @DisplayName("При запросе всех бронирований владельца со статусом CURRENT должен вернуть список")
    void test_getAllUserBookings_WhenCurrentShouldReturnBooking() {
        //given
        Mockito.
                when(bookingRepository.findCurrentByOwnerId(OWNER_ID))
                .thenReturn(List.of(booking));

        //when
        List<BookingDto> result = bookingService.getAllUserBookings(OWNER_ID, STATE_CURRENT);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getStatus()).isEqualTo(String.valueOf(STATUS_WAITING));
        assertThat(result.get(0).getBooker().getId()).isEqualTo(BOOKER_ID);
        assertThat(result.get(0).getItem().getId()).isEqualTo(ITEM_ID);
        assertThat(result.get(0).getItem().getAvailable()).isEqualTo(ITEM_AVAILABLE);

        verify(bookingRepository, times(1)).findCurrentByOwnerId(OWNER_ID);
    }

    @Test
    @DisplayName("При запросе всех бронирований владельца со статусом FUTURE должен вернуть список")
    void test_getAllUserBookings_WhenFutureShouldReturnBooking() {
        //given
        Mockito.
                when(bookingRepository.findFutureByOwnerId(OWNER_ID))
                .thenReturn(List.of(booking));

        //when
        List<BookingDto> result = bookingService.getAllUserBookings(OWNER_ID, STATE_FUTURE);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getStatus()).isEqualTo(String.valueOf(STATUS_WAITING));
        assertThat(result.get(0).getBooker().getId()).isEqualTo(BOOKER_ID);
        assertThat(result.get(0).getItem().getId()).isEqualTo(ITEM_ID);
        assertThat(result.get(0).getItem().getAvailable()).isEqualTo(ITEM_AVAILABLE);

        verify(bookingRepository, times(1)).findFutureByOwnerId(OWNER_ID);
    }

    @Test
    @DisplayName("При запросе всех бронирований владельца со статусом PAST должен вернуть список")
    void test_getAllUserBookings_WhenPastShouldReturnBooking() {
        //given
        Mockito.
                when(bookingRepository.findPastByOwnerId(OWNER_ID))
                .thenReturn(List.of(booking));

        //when
        List<BookingDto> result = bookingService.getAllUserBookings(OWNER_ID, STATE_PAST);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getStatus()).isEqualTo(String.valueOf(STATUS_WAITING));
        assertThat(result.get(0).getBooker().getId()).isEqualTo(BOOKER_ID);
        assertThat(result.get(0).getItem().getId()).isEqualTo(ITEM_ID);
        assertThat(result.get(0).getItem().getAvailable()).isEqualTo(ITEM_AVAILABLE);

        verify(bookingRepository, times(1)).findPastByOwnerId(OWNER_ID);
    }
}