package ru.practicum.shareit.request;

import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoGet;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.user.User;

import java.util.List;

public class ItemRequestMapper {
    public static ItemRequest toItemRequest(NewItemRequestDto newItemRequestDto, User requestor) {
        return ItemRequest.builder()
                .description(newItemRequestDto.getDescription())
                .requestor(requestor)
                .created(newItemRequestDto.getCreated())
                .build();
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .build();
    }

    public static ItemRequestDtoGet toItemRequestDtoGet(ItemRequest itemRequest, List<Item> items) {
        return ItemRequestDtoGet.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .items(items.stream().map(ItemMapper::toItemForItemRequestDto).toList())
                .build();
    }
}
