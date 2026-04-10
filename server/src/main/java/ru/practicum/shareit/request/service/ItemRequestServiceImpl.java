package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoGet;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto addItemRequest(Long userId, NewItemRequestDto newItemRequestDto) {
        log.info("Началось добавление запроса на добавление вещи от пользователя {}", userId);
        ItemRequest itemRequest = getValitItemRequest(userId, newItemRequestDto);
        log.info("Входной запрос на добавление запроса преобразован в соответствующий запрос");
        return ItemRequestMapper.toItemRequestDto(itemRequestRepository.save(itemRequest));

    }

    @Override
    public List<ItemRequestDtoGet> getAllUserItemRequests(Long userId) {
        log.info("Начался процесс получения всех запросов пользователя {}", userId);
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId);
        log.info("Найдено запросов для пользователя {}: {}", userId, itemRequests.size());

        List<Long> requestsIds = itemRequests.stream().map(ItemRequest::getId).toList();
        log.info("Id запросов для поиска вещей: {}", requestsIds);

        Map<Long, List<Item>> itemsByRequests = itemRepository.findAllByRequestIdIn(requestsIds).stream()
                .filter(i -> i.getRequest() != null)
                .collect(Collectors.groupingBy(i -> i.getRequest().getId()));
        log.info("Найдено вещей, привязанных к запросам: {}", itemsByRequests.size());

        List<ItemRequestDtoGet> result = itemRequests.stream()
                .map(itemRequest -> ItemRequestMapper.toItemRequestDtoGet(itemRequest,
                        itemsByRequests.getOrDefault(itemRequest.getId(), Collections.emptyList())))
                .toList();
        log.info("Успешно сформирован список запросов для пользователя {}, количество: {}", userId, result.size());
        return result;
    }

    @Override
    public List<ItemRequestDto> getAllOtherItemRequests(Long userId) {
        log.info("Начался процесс получения всех чужих запросов для пользователя {}", userId);
        List<ItemRequestDto> result = itemRequestRepository.findAllRequestsByOtherUsers(userId)
                .stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .toList();
        log.info("Найдено чужих запросов для пользователя {}: {}", userId, result.size());
        return result;
    }

    @Override
    public ItemRequestDtoGet getItemRequest(Long requestId) {
        log.info("Начался процесс получения запроса по id {}", requestId);

        ItemRequest itemRequest = itemRequestRepository.findById(requestId).orElseThrow(() -> {
            String requestNotFound = "Запрос " + requestId + "не найден";
            log.error(requestNotFound);
            return new NotFoundException(requestNotFound);
        });
        log.info("Запрос с id {} найден", requestId);

        List<Item> itemsByRequest = itemRepository.findAllByRequestId(requestId);
        log.info("Для запроса {} найдено вещей-ответов: {}", requestId, itemsByRequest.size());

        return ItemRequestMapper.toItemRequestDtoGet(itemRequest, itemsByRequest);
    }

    /**
     * Преобразовывает DTO в валидный запрос
     */
    private ItemRequest getValitItemRequest(Long userId, NewItemRequestDto itemRequestDto) {
        log.info("Начался внутренний процесс преобразования DTO-запроса в запрос от пользователя {}", userId);
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> {
                    String userNotFound = "Пользователь с id " + userId +
                            "не найден";
                    log.error(userNotFound);
                    return new NotFoundException(userNotFound);
                });
        log.info("Пользователь {} найден, создаём запрос", userId);

        itemRequestDto.setCreated(LocalDateTime.now());
        log.info("Установлено время создания запроса: {}", itemRequestDto.getCreated());

        return ItemRequestMapper.toItemRequest(itemRequestDto, requestor);
    }
}