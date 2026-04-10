package ru.practicum.shareit.item.dto;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class NewItemDtoTest {
    private final JacksonTester<NewItemDto> json;

    private static final Long ITEM_ID = 1L;
    private static final String ITEM_NAME = "Item";
    private static final String ITEM_DESCRIPTION = "Description";
    private static final Boolean AVAILABLE = true;
    private static final Long OWNER_ID = 100L;
    private static final Long REQUEST_ID = 200L;

    @Test
    @DisplayName("Проверяет сериализацию")
    void test_NewItemDto_WhenSerializeShouldReturnJson() throws Exception {
        //given
        NewItemDto dto = NewItemDto.builder()
                .id(ITEM_ID)
                .name(ITEM_NAME)
                .description(ITEM_DESCRIPTION)
                .available(AVAILABLE)
                .owner(OWNER_ID)
                .requestId(REQUEST_ID)
                .build();

        //when
        var result = json.write(dto);

        //then
        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).hasJsonPathStringValue("$.name");
        assertThat(result).hasJsonPathStringValue("$.description");
        assertThat(result).hasJsonPathBooleanValue("$.available");
        assertThat(result).hasJsonPathNumberValue("$.owner");
        assertThat(result).hasJsonPathNumberValue("$.requestId");
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(ITEM_NAME);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(ITEM_DESCRIPTION);
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(AVAILABLE);
        assertThat(result).extractingJsonPathNumberValue("$.owner").isEqualTo(OWNER_ID.intValue());
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(REQUEST_ID.intValue());
    }

    @Test
    @DisplayName("Проверяет десериализацию со всеми полями")
    void test_NewItemDto_WhenDeserializeShouldReturnDto() throws Exception {
        //given
        String content = String.format("""
                {
                    "id": %d,
                    "name": "%s",
                    "description": "%s",
                    "available": %s,
                    "owner": %d,
                    "requestId": %d
                }
                """, ITEM_ID, ITEM_NAME, ITEM_DESCRIPTION, AVAILABLE, OWNER_ID, REQUEST_ID);

        //when
        NewItemDto dto = json.parse(content).getObject();

        //then
        assertThat(dto.getId()).isEqualTo(ITEM_ID);
        assertThat(dto.getName()).isEqualTo(ITEM_NAME);
        assertThat(dto.getDescription()).isEqualTo(ITEM_DESCRIPTION);
        assertThat(dto.getAvailable()).isEqualTo(AVAILABLE);
        assertThat(dto.getOwner()).isEqualTo(OWNER_ID);
        assertThat(dto.getRequestId()).isEqualTo(REQUEST_ID);
    }

    @Test
    @DisplayName("Проверяет десериализацию только с обязательными полями")
    void test_NewItemDto_WhenWithoutOptionalFieldsShouldDeserialize() throws Exception {
        //given
        String content = String.format("""
                {
                    "name": "%s",
                    "description": "%s",
                    "available": %s
                }
                """, ITEM_NAME, ITEM_DESCRIPTION, AVAILABLE);

        //when
        NewItemDto dto = json.parse(content).getObject();

        //then
        assertThat(dto.getId()).isNull();
        assertThat(dto.getName()).isEqualTo(ITEM_NAME);
        assertThat(dto.getDescription()).isEqualTo(ITEM_DESCRIPTION);
        assertThat(dto.getAvailable()).isEqualTo(AVAILABLE);
        assertThat(dto.getOwner()).isNull();
        assertThat(dto.getRequestId()).isNull();
    }
}
