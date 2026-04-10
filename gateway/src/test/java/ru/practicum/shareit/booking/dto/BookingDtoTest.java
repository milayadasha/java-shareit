package ru.practicum.shareit.booking.dto;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.user.dto.UserBookingDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingDtoTest {
    private final JacksonTester<BookingDto> json;

    private static final Long BOOKING_ID = 1L;
    private static final LocalDateTime START =
            LocalDateTime.of(2026, 4, 10, 12, 0, 0);
    private static final LocalDateTime END =
            LocalDateTime.of(2026, 4, 12, 12, 0, 0);
    private static final String START_STRING = START.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    private static final String END_STRING = END.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    private static final String STATUS = "WAITING";

    private static final Long ITEM_ID = 10L;
    private static final String ITEM_NAME = "Item";
    private static final String ITEM_DESCRIPTION = "Item Description";
    private static final Boolean ITEM_AVAILABLE = true;
    private static final Long OWNER_ID = 100L;

    private static final Long BOOKER_ID = 200L;
    private static final String BOOKER_NAME = "Booker";

    @Test
    @DisplayName("Проверяет сериализация")
    void test_BookingDto_WHenSerializeShouldReturnJson() throws Exception {
        //given
        ItemBookingDto item = ItemBookingDto.builder()
                .id(ITEM_ID)
                .name(ITEM_NAME)
                .description(ITEM_DESCRIPTION)
                .available(ITEM_AVAILABLE)
                .owner(OWNER_ID)
                .build();

        UserBookingDto booker = new UserBookingDto(BOOKER_ID, BOOKER_NAME);

        BookingDto dto = BookingDto.builder()
                .id(BOOKING_ID)
                .start(START)
                .end(END)
                .item(item)
                .booker(booker)
                .status(STATUS)
                .build();

        //when
        var result = json.write(dto);

        //then
        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).hasJsonPathStringValue("$.start");
        assertThat(result).hasJsonPathStringValue("$.end");
        assertThat(result).hasJsonPathStringValue("$.status");
        assertThat(result).hasJsonPathValue("$.item");
        assertThat(result).hasJsonPathValue("$.booker");
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(START_STRING);
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(END_STRING);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo(STATUS);
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(ITEM_ID.intValue());
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo(ITEM_NAME);
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(BOOKER_ID.intValue());
        assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo(BOOKER_NAME);
    }

    @Test
    @DisplayName("Проверяет десериализацию")
    void testDeserialize_shouldReturnDto() throws Exception {
        //given
        String content = String.format("""
                        {
                            "id": %d,
                            "start": "%s",
                            "end": "%s",
                            "status": "%s",
                            "item": {
                                "id": %d,
                                "name": "%s",
                                "description": "%s",
                                "available": %s,
                                "owner": %d
                            },
                            "booker": {
                                "id": %d,
                                "name": "%s"
                            }
                        }
                        """, BOOKING_ID, START_STRING, END_STRING, STATUS,
                ITEM_ID, ITEM_NAME, ITEM_DESCRIPTION, ITEM_AVAILABLE, OWNER_ID,
                BOOKER_ID, BOOKER_NAME);

        //when
        BookingDto dto = json.parse(content).getObject();

        //then
        assertThat(dto.getId()).isEqualTo(BOOKING_ID);
        assertThat(dto.getStart()).isEqualTo(START);
        assertThat(dto.getEnd()).isEqualTo(END);
        assertThat(dto.getStatus()).isEqualTo(STATUS);
        assertThat(dto.getItem().getId()).isEqualTo(ITEM_ID);
        assertThat(dto.getItem().getName()).isEqualTo(ITEM_NAME);
        assertThat(dto.getBooker().getId()).isEqualTo(BOOKER_ID);
        assertThat(dto.getBooker().getName()).isEqualTo(BOOKER_NAME);
    }
}