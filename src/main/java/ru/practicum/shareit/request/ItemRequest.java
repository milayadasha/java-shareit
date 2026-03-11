package ru.practicum.shareit.request;

import lombok.*;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */
@Data
@Builder(toBuilder = true)
@EqualsAndHashCode(of = { "id" })
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequest {
    private Long id;
    private String description;
    private Long requestor;
    private LocalDateTime created;

    public ItemRequest(Long id) {
        this.id = id;
    }
}
