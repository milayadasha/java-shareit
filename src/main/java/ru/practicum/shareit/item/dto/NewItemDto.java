package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class NewItemDto {
    private Long id;
    @NotNull
    private String name;
    @NotNull
    private String description;

    @NotNull
    private Boolean available;

    private Long owner;
    private Long request;
}
