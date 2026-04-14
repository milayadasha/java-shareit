package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoGet;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    @Autowired
    @Qualifier("itemRequestServiceImpl")
    private ItemRequestService itemRequestsService;

    /**
     * Добавляет новый запрос на добавление вещи
     */
    @PostMapping
    public ResponseEntity<ItemRequestDto> addItemRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                         @RequestBody NewItemRequestDto newItemRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(itemRequestsService.addItemRequest(userId, newItemRequestDto));
    }

    /**
     * Возвращает запрос на добавление вещи по id
     */
    @GetMapping("/{id}")
    public ResponseEntity<ItemRequestDtoGet> getItemRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(itemRequestsService.getItemRequest(id));
    }

    /**
     * Возвращает запросы на добавление вещей конкретного пользователя
     */
    @GetMapping
    public ResponseEntity<List<ItemRequestDtoGet>> getAllItemRequestsByUser(
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return ResponseEntity.ok(itemRequestsService.getAllUserItemRequests(userId));
    }

    /**
     * Возвращает запросы на добавление вещей всех остальных пользователей
     */
    @GetMapping("/all")
    public ResponseEntity<List<ItemRequestDto>> getAllItemRequestsByOther(
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return ResponseEntity.ok(itemRequestsService.getAllOtherItemRequests(userId));
    }
}
