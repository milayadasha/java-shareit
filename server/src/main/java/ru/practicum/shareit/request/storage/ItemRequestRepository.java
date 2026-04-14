package ru.practicum.shareit.request.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.ItemRequest;

import java.util.List;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    @Query(" SELECT r FROM ItemRequest r " +
            "WHERE r.requestor.id != ?1 " +
            "ORDER BY r.created DESC")
    List<ItemRequest> findAllRequestsByOtherUsers(Long userId);

    List<ItemRequest> findAllByRequestorIdOrderByCreatedDesc(Long userId);
}

