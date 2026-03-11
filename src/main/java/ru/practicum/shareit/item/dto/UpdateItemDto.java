package ru.practicum.shareit.item.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateItemDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
}
