package ru.practicum.shareit.item.dto;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingDtoShort;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemDtoGetByIdTest {
    private final JacksonTester<ItemDtoGetById> json;

    private static final Long ITEM_ID = 1L;
    private static final String ITEM_NAME = "Item";
    private static final String ITEM_DESCRIPTION = "Item description";
    private static final Boolean ITEM_AVAILABLE = true;

    private static final Long LAST_BOOKING_ID = 5L;
    private static final Long LAST_BOOKER_ID = 6L;

    private static final LocalDateTime LAST_START =
            LocalDateTime.of(2026, 4, 5, 10, 0, 0);
    private static final LocalDateTime LAST_END =
            LocalDateTime.of(2026, 4, 7, 10, 0, 0);
    private static final String LAST_START_STRING = LAST_START.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    private static final String LAST_END_STRING = LAST_END.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    private static final Long NEXT_BOOKING_ID = 10L;
    private static final Long NEXT_BOOKER_ID = 20L;

    private static final LocalDateTime NEXT_START =
            LocalDateTime.of(2026, 4, 15, 10, 0, 0);
    private static final LocalDateTime NEXT_END =
            LocalDateTime.of(2026, 4, 17, 10, 0, 0);

    private static final String NEXT_START_STRING = NEXT_START.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    private static final String NEXT_END_STRING = NEXT_END.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    private static final Long COMMENT_ID = 10L;
    private static final String COMMENT_TEXT = "Great item!";
    private static final String AUTHOR_NAME = "John Doe";
    private static final LocalDateTime CREATED = LocalDateTime.of(2026, 4, 10, 12, 0, 0);
    private static final String CREATED_STRING = CREATED.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    @Test
    @DisplayName("Проверяет сериализацию")
    void test_ItemDtoGetById_WhenSerializeShouldReturnJson() throws Exception {
        //given
        BookingDtoShort lastBooking = BookingDtoShort.builder()
                .id(LAST_BOOKING_ID)
                .bookerId(LAST_BOOKER_ID)
                .start(LAST_START)
                .end(LAST_END)
                .build();

        BookingDtoShort nextBooking = BookingDtoShort.builder()
                .id(NEXT_BOOKING_ID)
                .bookerId(NEXT_BOOKER_ID)
                .start(NEXT_START)
                .end(NEXT_END)
                .build();

        CommentDto comment = CommentDto.builder()
                .id(COMMENT_ID)
                .text(COMMENT_TEXT)
                .authorName(AUTHOR_NAME)
                .created(CREATED)
                .build();

        ItemDtoGetById dto = ItemDtoGetById.builder()
                .id(ITEM_ID)
                .name(ITEM_NAME)
                .description(ITEM_DESCRIPTION)
                .available(ITEM_AVAILABLE)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(List.of(comment))
                .build();

        //when
        var result = json.write(dto);

        //then
        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).hasJsonPathStringValue("$.name");
        assertThat(result).hasJsonPathStringValue("$.description");
        assertThat(result).hasJsonPathBooleanValue("$.available");
        assertThat(result).hasJsonPathValue("$.lastBooking");
        assertThat(result).hasJsonPathValue("$.nextBooking");
        assertThat(result).hasJsonPathArrayValue("$.comments");
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(ITEM_NAME);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(ITEM_DESCRIPTION);
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(ITEM_AVAILABLE);
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.id")
                .isEqualTo(LAST_BOOKING_ID.intValue());
        assertThat(result).extractingJsonPathStringValue("$.lastBooking.start").isEqualTo(LAST_START_STRING);
        assertThat(result).extractingJsonPathStringValue("$.lastBooking.end").isEqualTo(LAST_END_STRING);
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.id")
                .isEqualTo(NEXT_BOOKING_ID.intValue());
        assertThat(result).extractingJsonPathStringValue("$.nextBooking.start").isEqualTo(NEXT_START_STRING);
        assertThat(result).extractingJsonPathStringValue("$.nextBooking.end").isEqualTo(NEXT_END_STRING);
        assertThat(result).extractingJsonPathNumberValue("$.comments[0].id").isEqualTo(COMMENT_ID.intValue());
        assertThat(result).extractingJsonPathStringValue("$.comments[0].created").isEqualTo(CREATED_STRING);
    }

    @Test
    @DisplayName("Проверяет десериализацию")
    void test_ItemDtoGetById_WhenDeserializeShouldReturnDto() throws Exception {
        //given
        String content = String.format("""
                        {
                            "id": %d,
                            "name": "%s",
                            "description": "%s",
                            "available": %s,
                            "lastBooking": {
                                "id": %d,
                                "bookerId": %d,
                                "start": "%s",
                                "end": "%s"
                            },
                            "nextBooking": {
                                "id": %d,
                                "bookerId": %d,
                                "start": "%s",
                                "end": "%s"
                            },
                            "comments": [
                                {
                                    "id": %d,
                                    "text": "%s",
                                    "authorName": "%s",
                                    "created": "%s"
                                }
                            ]
                        }
                        """, ITEM_ID, ITEM_NAME, ITEM_DESCRIPTION, ITEM_AVAILABLE,
                LAST_BOOKING_ID, LAST_BOOKER_ID, LAST_START_STRING, LAST_END_STRING,
                NEXT_BOOKING_ID, NEXT_BOOKER_ID, NEXT_START_STRING, NEXT_END_STRING,
                COMMENT_ID, COMMENT_TEXT, AUTHOR_NAME, CREATED_STRING);

        //when
        ItemDtoGetById dto = json.parse(content).getObject();

        //then
        assertThat(dto.getId()).isEqualTo(ITEM_ID);
        assertThat(dto.getName()).isEqualTo(ITEM_NAME);
        assertThat(dto.getDescription()).isEqualTo(ITEM_DESCRIPTION);
        assertThat(dto.getAvailable()).isEqualTo(ITEM_AVAILABLE);
        assertThat(dto.getLastBooking().getId()).isEqualTo(LAST_BOOKING_ID);
        assertThat(dto.getLastBooking().getBookerId()).isEqualTo(LAST_BOOKER_ID);
        assertThat(dto.getLastBooking().getStart()).isEqualTo(LAST_START);
        assertThat(dto.getLastBooking().getEnd()).isEqualTo(LAST_END);
        assertThat(dto.getNextBooking().getId()).isEqualTo(NEXT_BOOKING_ID);
        assertThat(dto.getNextBooking().getBookerId()).isEqualTo(NEXT_BOOKER_ID);
        assertThat(dto.getNextBooking().getStart()).isEqualTo(NEXT_START);
        assertThat(dto.getNextBooking().getEnd()).isEqualTo(NEXT_END);
        assertThat(dto.getComments()).hasSize(1);
        assertThat(dto.getComments().get(0).getId()).isEqualTo(COMMENT_ID);
        assertThat(dto.getComments().get(0).getText()).isEqualTo(COMMENT_TEXT);
        assertThat(dto.getComments().get(0).getAuthorName()).isEqualTo(AUTHOR_NAME);
        assertThat(dto.getComments().get(0).getCreated()).isEqualTo(CREATED);
    }
}
