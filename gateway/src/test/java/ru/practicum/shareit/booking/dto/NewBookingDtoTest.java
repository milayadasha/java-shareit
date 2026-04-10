package ru.practicum.shareit.booking.dto;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class NewBookingDtoTest {
    private final JacksonTester<NewBookingDto> json;

    private static final Long ITEM_ID = 1L;
    private static final LocalDateTime START =
            LocalDateTime.of(2026, 4, 10, 12, 0, 0);
    private static final LocalDateTime END =
            LocalDateTime.of(2026, 4, 12, 12, 0, 0);
    private static final String START_STRING = START.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    private static final String END_STRING = END.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    private static final String content = """
            {
                "itemId": 1,
                "start": "2026-04-10T12:00:00",
                "end": "2026-04-12T12:00:00"
            }
            """;

    @Test
    @DisplayName("Проверяет сериализацию")
    void test_NewBookingDto_WhenSerializeShouldReturnJson() throws Exception {
        //given && when
        NewBookingDto dto = new NewBookingDto();
        dto.setItemId(ITEM_ID);
        dto.setStart(START);
        dto.setEnd(END);

        //then
        var result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.itemId");
        assertThat(result).hasJsonPathStringValue("$.start");
        assertThat(result).hasJsonPathStringValue("$.end");
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(START_STRING);
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(END_STRING);
    }

    @Test
    @DisplayName("Проверяет десериализацию")
    void test_NewBookingDto_WhenDeserializeShouldReturnDto() throws Exception {
        //given && when
        NewBookingDto dto = json.parse(content).getObject();

        //then
        assertThat(dto.getItemId()).isEqualTo(ITEM_ID);
        assertThat(dto.getStart()).isEqualTo(START);
        assertThat(dto.getEnd()).isEqualTo(END);
    }
}