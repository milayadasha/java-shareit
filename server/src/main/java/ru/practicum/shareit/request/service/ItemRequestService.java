package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoGet;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    /**
     * Добавляет новый запрос
     */
    ItemRequestDto addItemRequest(Long userId, NewItemRequestDto newItemRequestDto);

    /**
     * Возвращает все запросы пользователя
     */
    List<ItemRequestDtoGet> getAllUserItemRequests(Long userId);

    /**
     * Возвращает все запросы других пользователей
     */
    List<ItemRequestDto> getAllOtherItemRequests(Long userId);

    /**
     * Возвращает запрос пользователя по id
     */
    ItemRequestDtoGet getItemRequest(Long requestId);
}
