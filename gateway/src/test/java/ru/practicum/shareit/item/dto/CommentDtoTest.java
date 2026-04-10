package ru.practicum.shareit.item.dto;

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
public class CommentDtoTest {
    private final JacksonTester<CommentDto> json;

    private static final Long COMMENT_ID = 1L;
    private static final String TEXT = "CommentTest";
    private static final String AUTHOR_NAME = "Author";
    private static final LocalDateTime CREATED =
            LocalDateTime.of(2026, 4, 10, 12, 0, 0);
    private static final String CREATED_STRING = CREATED.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    @Test
    @DisplayName("Проверяет сериализацию")
    void test_CommentDto_WhenSerializeShouldReturnJson() throws Exception {
        //given
        CommentDto dto = CommentDto.builder()
                .id(COMMENT_ID)
                .text(TEXT)
                .authorName(AUTHOR_NAME)
                .created(CREATED)
                .build();

        //when
        var result = json.write(dto);

        //then
        assertThat(result).hasJsonPathStringValue("$.text");
        assertThat(result).hasJsonPathStringValue("$.authorName");
        assertThat(result).hasJsonPathStringValue("$.created");
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo(TEXT);
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo(AUTHOR_NAME);
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(CREATED_STRING);
    }

    @Test
    @DisplayName("Проверяет десериализацию")
    void test_CommentDto_WhenDeserializeShouldReturnDto() throws Exception {
        String content = String.format("""
                {
                    "id": %d,
                    "text": "%s",
                    "authorName": "%s",
                    "created": "%s"
                }
                """, COMMENT_ID, TEXT, AUTHOR_NAME, CREATED_STRING);

        CommentDto dto = json.parse(content).getObject();

        assertThat(dto.getId()).isEqualTo(COMMENT_ID);
        assertThat(dto.getText()).isEqualTo(TEXT);
        assertThat(dto.getAuthorName()).isEqualTo(AUTHOR_NAME);
        assertThat(dto.getCreated()).isEqualTo(CREATED);
    }
}
