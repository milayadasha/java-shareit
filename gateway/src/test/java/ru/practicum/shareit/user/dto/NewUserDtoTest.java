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
public class NewUserDtoTest {
    private final JacksonTester<NewUserDto> json;
    private static final String USER_NAME = "User";
    private static final String USER_EMAIL = "user@example.com";

    @Test
    @DisplayName("Проверяет сериализацию")
    void test_NewUserDto_WhenSerializeShouldReturnJson() throws Exception {
        //given
        NewUserDto dto = new NewUserDto();
        dto.setName(USER_NAME);
        dto.setEmail(USER_EMAIL);

        //when
        var result = json.write(dto);

        //then
        assertThat(result).hasJsonPathStringValue("$.name");
        assertThat(result).hasJsonPathStringValue("$.email");
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(USER_NAME);
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo(USER_EMAIL);
    }

    @Test
    @DisplayName("Проверяет десериализацию")
    void test_NewUserDto_WhenDeserializeShouldReturnDto() throws Exception {
        //given
        String content = String.format("{"
                + "\"name\": \"%s\","
                + "\"email\": \"%s\""
                + "}", USER_NAME, USER_EMAIL);

        //when
        NewUserDto dto = json.parse(content).getObject();

        //then
        assertThat(dto.getName()).isEqualTo(USER_NAME);
        assertThat(dto.getEmail()).isEqualTo(USER_EMAIL);
    }
}