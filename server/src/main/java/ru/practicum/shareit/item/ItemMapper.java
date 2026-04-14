package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;

import java.util.Collections;
import java.util.List;

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .owner(item.getOwner() != null ? item.getOwner().getId() : null)
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }

    public static Item toItem(NewItemDto itemDto, ItemRequest itemRequest) {
        return Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .request(itemRequest)
                .build();
    }

    public static Item toItem(UpdateItemDto itemDto) {
        Item item = Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .build();

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return item;
    }

    public static ItemBookingDto toItemBookingDto(Item item) {
        return ItemBookingDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .owner(item.getOwner() != null ? item.getOwner().getId() : null)
                .build();
    }

    public static ItemDtoGetById toItemDtoForGetById(Item item, Booking lastBooking, Booking nextBooking,
                                                     List<Comment> comments) {
        return ItemDtoGetById.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lastBooking != null ? BookingMapper.toBookingDtoShort(lastBooking) : null)
                .nextBooking(nextBooking != null ? BookingMapper.toBookingDtoShort(nextBooking) : null)
                .comments(comments.isEmpty() ? Collections.emptyList() :
                        comments.stream().map(CommentMapper::toCommentDto).toList())
                .build();
    }

    public static ItemDtoGet toItemDtoGet(Item item, List<Comment> comments) {
        return ItemDtoGet.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .owner(item.getOwner() != null ? item.getOwner().getId() : null)
                .request(item.getRequest() != null ? item.getRequest().getId() : null)
                .comments(comments.isEmpty() ? Collections.emptyList() :
                        comments.stream().map(CommentMapper::toCommentDto).toList())
                .build();
    }

    public static ItemForItemRequestDto toItemForItemRequestDto(Item item) {
        return ItemForItemRequestDto.builder()
                .id(item.getId())
                .name(item.getName())
                .ownerId(item.getOwner() != null ? item.getOwner().getId() : null)
                .build();
    }
}
