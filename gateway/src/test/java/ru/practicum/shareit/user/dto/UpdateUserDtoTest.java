package ru.practicum.shareit.user.dto;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UpdateUserDtoTest {
    private final JacksonTester<UpdateUserDto> json;

    private static final Long USER_ID = 1L;
    private static final String UPDATED_NAME = "Updated Name";
    private static final String UPDATED_EMAIL = "updated@example.com";

    @Test
    @DisplayName("Проверяет сериализацию")
    void test_UpdateUserDto_WhenSerializeShouldReturnJson() throws Exception {
        //given
        UpdateUserDto dto = new UpdateUserDto();
        dto.setId(USER_ID);
        dto.setName(UPDATED_NAME);
        dto.setEmail(UPDATED_EMAIL);

        //when
        var result = json.write(dto);

        //then
        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).hasJsonPathStringValue("$.name");
        assertThat(result).hasJsonPathStringValue("$.email");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(USER_ID.intValue());
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(UPDATED_NAME);
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo(UPDATED_EMAIL);
    }

    @Test
    @DisplayName("Проверяет десериализацию")
    void test_UpdateUserDto_WhenDeserializeShouldReturnDto() throws Exception {
        //given
        String content = String.format("{"
                + "\"id\": %s,"
                + "\"name\": \"%s\","
                + "\"email\": \"%s\""
                + "}", USER_ID, UPDATED_NAME, UPDATED_EMAIL);

        //when
        UpdateUserDto dto = json.parse(content).getObject();

        //then
        assertThat(dto.getId()).isEqualTo(USER_ID.intValue());
        assertThat(dto.getName()).isEqualTo(UPDATED_NAME);
        assertThat(dto.getEmail()).isEqualTo(UPDATED_EMAIL);
    }

    @Test
    @DisplayName("Проверяет десериализацию, когда обновляется только имя")
    void test_UpdateUserDto_WhenOnlyNameProvidedShouldDeserialize() throws Exception {
        //given
        String content = String.format("{\"name\": \"%s\"}", UPDATED_NAME);

        //when
        UpdateUserDto dto = json.parse(content).getObject();

        //then
        assertThat(dto.getName()).isEqualTo(UPDATED_NAME);
        assertThat(dto.getEmail()).isNull();
    }

    @Test
    @DisplayName("Проверяет десериализацию, когда обновляется только email")
    void test_UpdateUserDto_WhenOnlyEmailProvidedShouldDeserialize() throws Exception {
        //given
        String content = String.format("{\"email\": \"%s\"}", UPDATED_EMAIL);

        //when
        UpdateUserDto dto = json.parse(content).getObject();

        //then
        assertThat(dto.getName()).isNull();
        assertThat(dto.getEmail()).isEqualTo(UPDATED_EMAIL);
    }
}