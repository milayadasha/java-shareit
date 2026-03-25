package ru.practicum.shareit.user;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder(toBuilder = true)
@EqualsAndHashCode(of = { "id" })
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;
    private String name;

    @NotNull
    private String email;
}
