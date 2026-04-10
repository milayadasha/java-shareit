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
public class NewItemRequestDtoTest {
    private final JacksonTester<NewItemRequestDto> json;

    private static final String DESCRIPTION = "Description";
    private static final LocalDateTime CREATED =
            LocalDateTime.of(2026, 4, 10, 12, 0, 0);
    private static final String CREATED_STRING = CREATED.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    @Test
    @DisplayName("Проверяет сериализацию")
    void test_NewItemRequestDto_WhenSerializeShouldReturnJson() throws Exception {
        //given
        NewItemRequestDto dto = new NewItemRequestDto();
        dto.setDescription(DESCRIPTION);
        dto.setCreated(CREATED);

        //when
        var result = json.write(dto);

        //then
        assertThat(result).hasJsonPathStringValue("$.description");
        assertThat(result).hasJsonPathStringValue("$.created");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(DESCRIPTION);
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(CREATED_STRING);
    }

    @Test
    @DisplayName("Проверяет десериализацию")
    void test_NewItemRequestDto_WhenDeserializeShouldReturnDto() throws Exception {
        //given
        String content = String.format("""
                {
                    "description": "%s",
                    "created": "%s"
                }
                """, DESCRIPTION, CREATED_STRING);

        //when
        NewItemRequestDto dto = json.parse(content).getObject();

        //then
        assertThat(dto.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(dto.getCreated()).isEqualTo(CREATED
        );
    }
}
