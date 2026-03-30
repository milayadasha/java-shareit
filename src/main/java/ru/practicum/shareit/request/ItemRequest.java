package ru.practicum.shareit.request;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */
@Entity
@Table(name = "requests")
@Getter
@Setter
@Builder(toBuilder = true)
@EqualsAndHashCode(of = { "id" })
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;

    @Column(name = "requestor_id")
    private Long requestor;

    @Column(name = "created_at")
    private LocalDateTime created;

    public ItemRequest(Long id) {
        this.id = id;
    }
}
