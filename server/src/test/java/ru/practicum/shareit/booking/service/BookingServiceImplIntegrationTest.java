package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
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
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplIntegrationTest {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingService bookingService;

    private User owner;
    private User booker;
    private Item item;

    private static final String OWNER_NAME = "Owner";
    private static final String OWNER_EMAIL = "owner@test.com";
    private static final String BOOKER_NAME = "Booker";
    private static final String BOOKER_EMAIL = "booker@test.com";
    private static final String ITEM_NAME = "Item";
    private static final String ITEM_DESCRIPTION = "Item description";

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
        owner = new User();
        owner.setName(OWNER_NAME);
        owner.setEmail(OWNER_EMAIL);
        owner = userRepository.save(owner);

        booker = new User();
        booker.setName(BOOKER_NAME);
        booker.setEmail(BOOKER_EMAIL);
        booker = userRepository.save(booker);

        item = new Item();
        item.setName(ITEM_NAME);
        item.setDescription(ITEM_DESCRIPTION);
        item.setAvailable(ITEM_AVAILABLE);
        item.setOwner(owner);
        item = itemRepository.save(item);
    }

    @Test
    @DisplayName("При добавлении бронирования должен сохранить и вернуть бронирование")
    void test_addBooking_WhenAddShouldReturnOne() {
        //given
        NewBookingDto newBookingDto = new NewBookingDto();
        newBookingDto.setItemId(item.getId());
        newBookingDto.setStart(LocalDateTime.now().plusDays(1));
        newBookingDto.setEnd(LocalDateTime.now().plusDays(2));

        //when
        BookingDto result = bookingService.addBooking(booker.getId(), newBookingDto);

        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(STATUS_WAITING.name());
        assertThat(result.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(result.getItem().getId()).isEqualTo(item.getId());
        assertThat(result.getStart()).isNotNull();
        assertThat(result.getEnd()).isNotNull();

        Optional<Booking> savedBooking = bookingRepository.findById(result.getId());
        assertNotNull(savedBooking, "Бронирование не сохранено в БД");
    }

    @Test
    @DisplayName("При подтверждении бронирования должен изменить статус на APPROVED")
    void test_approveBooking_WhenWaitStatusReturnApproved() {
        //given
        NewBookingDto newBookingDto = new NewBookingDto();
        newBookingDto.setItemId(item.getId());
        newBookingDto.setStart(LocalDateTime.now().plusDays(1));
        newBookingDto.setEnd(LocalDateTime.now().plusDays(2));

        //when
        BookingDto bookingDto = bookingService.addBooking(booker.getId(), newBookingDto);
        BookingDto approvedBookingDto = bookingService
                .approveBooking(owner.getId(), bookingDto.getId(), BOOKING_APPROVED);

        //then
        assertThat(approvedBookingDto.getId()).isNotNull();
        assertThat(approvedBookingDto.getStatus()).isEqualTo(STATUS_APPROVED.name());

        Optional<Booking> savedBooking = bookingRepository.findById(approvedBookingDto.getId());
        assertNotNull(savedBooking, "Бронирование не сохранено в БД");
    }

    @Test
    @DisplayName("При запросе всех бронирований пользователя со статусом ALL должен вернуть список")
    void test_getAllBookingsByUser_WhenAllShouldReturnBooking() {
        //given
        NewBookingDto newBookingDto = new NewBookingDto();
        newBookingDto.setItemId(item.getId());
        newBookingDto.setStart(LocalDateTime.now().plusDays(1));
        newBookingDto.setEnd(LocalDateTime.now().plusDays(2));

        //when
        BookingDto bookingDto = bookingService.addBooking(booker.getId(), newBookingDto);
        List<BookingDto> result = bookingService.getAllBookingsByUser(booker.getId(), STATE_ALL);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getId()).isEqualTo(bookingDto.getId());
        assertThat(result.get(0).getStatus()).isEqualTo(bookingDto.getStatus());
    }

    @Test
    @DisplayName("При запросе всех бронирований пользователя со статусом WAITING должен вернуть список")
    void test_getAllBookingsByUser_WhenWaitingShouldReturnBooking() {
        //given
        NewBookingDto newBookingDto = new NewBookingDto();
        newBookingDto.setItemId(item.getId());
        newBookingDto.setStart(LocalDateTime.now().plusDays(1));
        newBookingDto.setEnd(LocalDateTime.now().plusDays(2));

        //when
        BookingDto bookingDto = bookingService.addBooking(booker.getId(), newBookingDto);
        List<BookingDto> result = bookingService.getAllBookingsByUser(booker.getId(), STATE_WAITING);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getId()).isEqualTo(bookingDto.getId());
        assertThat(result.get(0).getStatus()).isEqualTo(bookingDto.getStatus());
    }

    @Test
    @DisplayName("При запросе всех бронирований пользователя со статусом CURRENT должен вернуть список")
    void test_getAllBookingsByUser_WhenCurrentShouldReturnBooking() {
        //given
        NewBookingDto newBookingDto = new NewBookingDto();
        newBookingDto.setItemId(item.getId());
        newBookingDto.setStart(LocalDateTime.now());
        newBookingDto.setEnd(LocalDateTime.now().plusDays(2));

        //when
        BookingDto bookingDto = bookingService.addBooking(booker.getId(), newBookingDto);
        List<BookingDto> result = bookingService.getAllBookingsByUser(booker.getId(), STATE_CURRENT);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getId()).isEqualTo(bookingDto.getId());
        assertThat(result.get(0).getStatus()).isEqualTo(bookingDto.getStatus());
    }

    @Test
    @DisplayName("При запросе всех бронирований пользователя со статусом FUTURE должен вернуть список")
    void test_getAllBookingsByUser_WhenFutureShouldReturnBooking() {
        //given
        NewBookingDto newBookingDto = new NewBookingDto();
        newBookingDto.setItemId(item.getId());
        newBookingDto.setStart(LocalDateTime.now().plusDays(1));
        newBookingDto.setEnd(LocalDateTime.now().plusDays(2));

        //when
        BookingDto bookingDto = bookingService.addBooking(booker.getId(), newBookingDto);
        List<BookingDto> result = bookingService.getAllBookingsByUser(booker.getId(), STATE_FUTURE);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getId()).isEqualTo(bookingDto.getId());
        assertThat(result.get(0).getStatus()).isEqualTo(bookingDto.getStatus());
    }

    @Test
    @DisplayName("При запросе всех бронирований пользователя со статусом PAST должен вернуть список")
    void test_getAllBookingsByUser_WhenPastShouldReturnBooking() {
        //given
        NewBookingDto newBookingDto = new NewBookingDto();
        newBookingDto.setItemId(item.getId());
        newBookingDto.setStart(LocalDateTime.now().minusDays(2));
        newBookingDto.setEnd(LocalDateTime.now().minusDays(1));

        //when
        BookingDto bookingDto = bookingService.addBooking(booker.getId(), newBookingDto);
        List<BookingDto> result = bookingService.getAllBookingsByUser(booker.getId(), STATE_PAST);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getId()).isEqualTo(bookingDto.getId());
        assertThat(result.get(0).getStatus()).isEqualTo(bookingDto.getStatus());
    }

    @Test
    @DisplayName("При запросе всех бронирований владельца со статусом ALL должен вернуть список")
    void test_getAllUserBookings_WhenAllShouldReturnBooking() {
        //given
        NewBookingDto newBookingDto = new NewBookingDto();
        newBookingDto.setItemId(item.getId());
        newBookingDto.setStart(LocalDateTime.now().plusDays(1));
        newBookingDto.setEnd(LocalDateTime.now().plusDays(2));

        //when
        BookingDto bookingDto = bookingService.addBooking(booker.getId(), newBookingDto);
        List<BookingDto> result = bookingService.getAllUserBookings(owner.getId(), STATE_ALL);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getId()).isEqualTo(bookingDto.getId());
        assertThat(result.get(0).getStatus()).isEqualTo(bookingDto.getStatus());
    }

    @Test
    @DisplayName("При запросе всех бронирований владельца со статусом WAITING должен вернуть список")
    void test_getAllUserBookings_WhenWaitingShouldReturnBooking() {
        //given
        NewBookingDto newBookingDto = new NewBookingDto();
        newBookingDto.setItemId(item.getId());
        newBookingDto.setStart(LocalDateTime.now().plusDays(1));
        newBookingDto.setEnd(LocalDateTime.now().plusDays(2));

        //when
        BookingDto bookingDto = bookingService.addBooking(booker.getId(), newBookingDto);
        List<BookingDto> result = bookingService.getAllUserBookings(owner.getId(), STATE_WAITING);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getId()).isEqualTo(bookingDto.getId());
        assertThat(result.get(0).getStatus()).isEqualTo(bookingDto.getStatus());
    }

    @Test
    @DisplayName("При запросе всех бронирований владельца со статусом CURRENT должен вернуть список")
    void test_getAllUserBookings_WhenCurrentShouldReturnBooking() {
        //given
        NewBookingDto newBookingDto = new NewBookingDto();
        newBookingDto.setItemId(item.getId());
        newBookingDto.setStart(LocalDateTime.now());
        newBookingDto.setEnd(LocalDateTime.now().plusDays(2));

        //when
        BookingDto bookingDto = bookingService.addBooking(booker.getId(), newBookingDto);
        List<BookingDto> result = bookingService.getAllUserBookings(owner.getId(), STATE_CURRENT);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getId()).isEqualTo(bookingDto.getId());
        assertThat(result.get(0).getStatus()).isEqualTo(bookingDto.getStatus());
    }

    @Test
    @DisplayName("При запросе всех бронирований владельца со статусом FUTURE должен вернуть список")
    void test_getAllUserBookings_WhenFutureShouldReturnBooking() {
        //given
        NewBookingDto newBookingDto = new NewBookingDto();
        newBookingDto.setItemId(item.getId());
        newBookingDto.setStart(LocalDateTime.now().plusDays(1));
        newBookingDto.setEnd(LocalDateTime.now().plusDays(2));

        //when
        BookingDto bookingDto = bookingService.addBooking(booker.getId(), newBookingDto);
        List<BookingDto> result = bookingService.getAllUserBookings(owner.getId(), STATE_FUTURE);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getId()).isEqualTo(bookingDto.getId());
        assertThat(result.get(0).getStatus()).isEqualTo(bookingDto.getStatus());
    }

    @Test
    @DisplayName("При запросе всех бронирований владельца со статусом PAST должен вернуть список")
    void test_getAllUserBookings_WhenPastShouldReturnBooking() {
        //given
        NewBookingDto newBookingDto = new NewBookingDto();
        newBookingDto.setItemId(item.getId());
        newBookingDto.setStart(LocalDateTime.now().minusDays(2));
        newBookingDto.setEnd(LocalDateTime.now().minusDays(1));

        //when
        BookingDto bookingDto = bookingService.addBooking(booker.getId(), newBookingDto);
        List<BookingDto> result = bookingService.getAllUserBookings(owner.getId(), STATE_PAST);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getId()).isEqualTo(bookingDto.getId());
        assertThat(result.get(0).getStatus()).isEqualTo(bookingDto.getStatus());
    }
}