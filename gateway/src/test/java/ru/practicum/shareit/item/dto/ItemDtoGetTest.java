package ru.practicum.shareit.item.dto;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemDtoGetTest {
    private final JacksonTester<ItemDtoGet> json;

    private static final Long ITEM_ID = 1L;
    private static final String ITEM_NAME = "Drill";
    private static final String ITEM_DESCRIPTION = "Powerful drill";
    private static final Boolean ITEM_AVAILABLE = true;
    private static final Long OWNER_ID = 100L;
    private static final Long REQUEST_ID = 200L;

    private static final Long COMMENT_ID = 10L;
    private static final String COMMENT_TEXT = "Great item!";
    private static final String AUTHOR_NAME = "John Doe";
    private static final LocalDateTime CREATED =
            LocalDateTime.of(2026, 4, 10, 12, 0, 0);
    private static final String CREATED_STRING = CREATED.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    @Test
    @DisplayName("Проверяет сериализацию")
    void test_ItemDtoGet_WhenSerializeShouldReturnJson() throws Exception {
        //given
        CommentDto comment = CommentDto.builder()
                .id(COMMENT_ID)
                .text(COMMENT_TEXT)
                .authorName(AUTHOR_NAME)
                .created(CREATED)
                .build();

        ItemDtoGet dto = ItemDtoGet.builder()
                .id(ITEM_ID)
                .name(ITEM_NAME)
                .description(ITEM_DESCRIPTION)
                .available(ITEM_AVAILABLE)
                .owner(OWNER_ID)
                .request(REQUEST_ID)
                .comments(List.of(comment))
                .build();

        //when
        var result = json.write(dto);

        //then
        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).hasJsonPathStringValue("$.name");
        assertThat(result).hasJsonPathStringValue("$.description");
        assertThat(result).hasJsonPathBooleanValue("$.available");
        assertThat(result).hasJsonPathNumberValue("$.owner");
        assertThat(result).hasJsonPathNumberValue("$.request");
        assertThat(result).hasJsonPathArrayValue("$.comments");
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(ITEM_NAME);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(ITEM_DESCRIPTION);
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(ITEM_AVAILABLE);
        assertThat(result).extractingJsonPathNumberValue("$.owner").isEqualTo(OWNER_ID.intValue());
        assertThat(result).extractingJsonPathNumberValue("$.request").isEqualTo(REQUEST_ID.intValue());
        assertThat(result).extractingJsonPathNumberValue("$.comments[0].id").isEqualTo(COMMENT_ID.intValue());
        assertThat(result).extractingJsonPathStringValue("$.comments[0].created").isEqualTo(CREATED_STRING);
    }

    @Test
    @DisplayName("Проверяет десериализацию")
    void testDeserialize_shouldReturnDto() throws Exception {
        //given
        String content = String.format("""
                        {
                            "id": %d,
                            "name": "%s",
                            "description": "%s",
                            "available": %s,
                            "owner": %d,
                            "request": %d,
                            "comments": [
                                {
                                    "id": %d,
                                    "text": "%s",
                                    "authorName": "%s",
                                    "created": "%s"
                                }
                            ]
                        }
                        """, ITEM_ID, ITEM_NAME, ITEM_DESCRIPTION, ITEM_AVAILABLE, OWNER_ID, REQUEST_ID,
                COMMENT_ID, COMMENT_TEXT, AUTHOR_NAME, CREATED_STRING);

        //when
        ItemDtoGet dto = json.parse(content).getObject();

        //then
        assertThat(dto.getId()).isEqualTo(ITEM_ID);
        assertThat(dto.getName()).isEqualTo(ITEM_NAME);
        assertThat(dto.getDescription()).isEqualTo(ITEM_DESCRIPTION);
        assertThat(dto.getAvailable()).isEqualTo(ITEM_AVAILABLE);
        assertThat(dto.getOwner()).isEqualTo(OWNER_ID);
        assertThat(dto.getRequest()).isEqualTo(REQUEST_ID);
        assertThat(dto.getComments()).hasSize(1);
        assertThat(dto.getComments().get(0).getId()).isEqualTo(COMMENT_ID);
        assertThat(dto.getComments().get(0).getText()).isEqualTo(COMMENT_TEXT);
        assertThat(dto.getComments().get(0).getAuthorName()).isEqualTo(AUTHOR_NAME);
        assertThat(dto.getComments().get(0).getCreated()).isEqualTo(CREATED);
    }
}
