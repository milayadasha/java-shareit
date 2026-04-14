package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByOwnerId(Long userId);

    @Query("SELECT i FROM Item i" +
            " WHERE (upper(i.name) LIKE upper(concat('%', ?1, '%'))" +
            " OR upper(i.description) LIKE upper(concat('%', ?1, '%')))" +
            " AND i.available = true")
    List<Item> getAvailableItemsBySearch(String text);

    List<Item> findAllByRequestIdIn(List<Long> requestIds);

    List<Item> findAllByRequestId(Long requestId);
}
