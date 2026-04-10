package ru.practicum.shareit.request.dto;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.dto.ItemForItemRequestDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestDtoGetTest {
    private final JacksonTester<ItemRequestDtoGet> json;

    private static final Long REQUEST_ID = 1L;
    private static final String DESCRIPTION = "Request description";
    private static final LocalDateTime CREATED =
            LocalDateTime.of(2026, 4, 10, 12, 0, 0);
    private static final String CREATED_STRING = CREATED.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    private static final Long ITEM_ID = 10L;
    private static final String ITEM_NAME = "Item";
    private static final Long OWNER_ID = 100L;

    @Test
    @DisplayName("Проверяет сериализацию")
    void test_ItemRequestDtoGet_WhenSerializeShouldReturnJson() throws IOException {
        //given
        ItemForItemRequestDto item = ItemForItemRequestDto.builder()
                .id(ITEM_ID)
                .name(ITEM_NAME)
                .ownerId(OWNER_ID)
                .build();

        ItemRequestDtoGet dto = ItemRequestDtoGet.builder()
                .id(REQUEST_ID)
                .description(DESCRIPTION)
                .created(CREATED)
                .items(List.of(item))
                .build();

        //when
        var result = json.write(dto);

        //then
        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).hasJsonPathStringValue("$.description");
        assertThat(result).hasJsonPathStringValue("$.created");
        assertThat(result).hasJsonPathArrayValue("$.items");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(DESCRIPTION);
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(CREATED_STRING);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(ITEM_ID.intValue());
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo(ITEM_NAME);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(OWNER_ID.intValue());
    }

    @Test
    @DisplayName("Проверяет десериализацию")
    void test_ItemRequestDtoGet_WhenDeserializeShouldReturnDto() throws Exception {
        //given
        String content = String.format("""
                        {
                            "id": %d,
                            "description": "%s",
                            "created": "%s",
                            "items": [
                                {
                                    "id": %d,
                                    "name": "%s",
                                    "ownerId": %d
                                }
                            ]
                        }
                        """, REQUEST_ID, DESCRIPTION, CREATED_STRING,
                ITEM_ID, ITEM_NAME, OWNER_ID);

        //when
        ItemRequestDtoGet dto = json.parse(content).getObject();

        //then
        assertThat(dto.getId()).isEqualTo(REQUEST_ID);
        assertThat(dto.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(dto.getCreated()).isEqualTo(CREATED);
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getId()).isEqualTo(ITEM_ID.intValue());
        assertThat(dto.getItems().get(0).getName()).isEqualTo(ITEM_NAME);
        assertThat(dto.getItems().get(0).getOwnerId()).isEqualTo(OWNER_ID.intValue());
    }
}

