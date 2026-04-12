package ru.practicum.shareit.request.dto;

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
public class ItemRequestDtoTest {
    private final JacksonTester<ItemRequestDto> json;

    private static final Long REQUEST_ID = 1L;
    private static final String DESCRIPTION = "Request description";
    private static final LocalDateTime CREATED =
            LocalDateTime.of(2026, 4, 10, 12, 0, 0);
    private static final String CREATED_STRING = CREATED.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    @Test
    @DisplayName("Проверяет сериализацию")
    void test_ItemRequestDto_WhenSerializeShouldReturnJson() throws Exception {
        //given
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(REQUEST_ID)
                .description(DESCRIPTION)
                .created(CREATED)
                .build();

        //when
        var result = json.write(dto);

        //then
        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).hasJsonPathStringValue("$.description");
        assertThat(result).hasJsonPathStringValue("$.created");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(DESCRIPTION);
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(CREATED_STRING);
    }

    @Test
    @DisplayName("Проверяет десериализацию")
    void test_ItemRequestDto_WhenDeserializeShouldReturnDto() throws Exception {
        //given
        String content = String.format("{"
                + "\"id\": %d,"
                + "\"description\": \"%s\","
                + "\"created\": \"%s\""
                + "}", REQUEST_ID, DESCRIPTION, CREATED_STRING);

        //when
        ItemRequestDto dto = json.parse(content).getObject();

        //then
        assertThat(dto.getId()).isEqualTo(REQUEST_ID);
        assertThat(dto.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(dto.getCreated()).isEqualTo(CREATED);
    }
}