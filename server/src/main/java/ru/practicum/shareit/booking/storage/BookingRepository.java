package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    @Query(" SELECT b FROM Booking b WHERE b.booker.id = ?1 " +
            "AND b.start <= CURRENT_TIMESTAMP AND b.end >= CURRENT_TIMESTAMP " +
            "ORDER BY b.start DESC")
    List<Booking> findCurrentByBookerId(Long bookerId);

    @Query(" SELECT b FROM Booking b WHERE b.booker.id = ?1 " +
            "AND b.start > CURRENT_TIMESTAMP AND b.end > CURRENT_TIMESTAMP " +
            "ORDER BY b.start DESC")
    List<Booking> findFutureByBookerId(Long bookerId);

    @Query(" SELECT b FROM Booking b WHERE b.booker.id = ?1 " +
            "AND b.start < CURRENT_TIMESTAMP AND b.end < CURRENT_TIMESTAMP " +
            "ORDER BY b.start DESC")
    List<Booking> findPastByBookerId(Long bookerId);

    List<Booking> findAllByItemOwnerIdOrderByStartDesc(Long ownerId);

    @Query("SELECT b FROM Booking b " +
            "JOIN b.item i " +
            "WHERE i.owner.id = ?1 " +
            "AND b.status LIKE ?2 " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByOwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status);

    @Query("SELECT b FROM Booking b " +
            "JOIN b.item i " +
            "WHERE i.owner.id = ?1 " +
            "AND b.start <= CURRENT_TIMESTAMP AND b.end >= CURRENT_TIMESTAMP " +
            "ORDER BY b.start DESC")
    List<Booking> findCurrentByOwnerId(Long ownerId);

    @Query("SELECT b FROM Booking b " +
            "JOIN b.item i " +
            "WHERE i.owner.id = ?1 " +
            "AND b.start > CURRENT_TIMESTAMP AND b.end > CURRENT_TIMESTAMP " +
            "ORDER BY b.start DESC")
    List<Booking> findFutureByOwnerId(Long ownerId);

    @Query("SELECT b FROM Booking b " +
            "JOIN b.item i " +
            "WHERE i.owner.id = ?1 " +
            "AND b.start < CURRENT_TIMESTAMP AND b.end < CURRENT_TIMESTAMP " +
            "ORDER BY b.start DESC")
    List<Booking> findPastByOwnerId(Long ownerId);

    @Query(" SELECT b FROM Booking b " +
            "WHERE b.booker.id = ?1 " +
            "AND b.item.id = ?2 " +
            "AND b.end < LOCALTIMESTAMP " +
            "AND b.status LIKE 'APPROVED' " +
            "ORDER BY b.end DESC")
    List<Booking> findPastByBookerIdAndItemIdAndStatusApproved(Long bookerId, Long itemId);

    @Query(" SELECT b FROM Booking b " +
            "JOIN b.item i " +
            "WHERE b.item.id = ?1 " +
            "AND b.status = 'APPROVED' " +
            "AND b.end < CURRENT_TIMESTAMP " +
            "AND i.owner.id = ?2 " +
            "ORDER BY b.end DESC " +
            "LIMIT 1")
    Optional<Booking> findLastApprovedBookingForOwner(Long itemId, Long ownerId);

    @Query(" SELECT b FROM Booking b " +
            "JOIN b.item i " +
            "WHERE b.item.id = ?1 " +
            "AND b.status = 'APPROVED' " +
            "AND b.start > CURRENT_TIMESTAMP " +
            "AND i.owner.id = ?2 " +
            "ORDER BY b.start ASC " +
            "LIMIT 1")
    Optional<Booking> findNextApprovedBookingForOwner(Long itemId, Long ownerId);

    @Query(value = "SELECT LOCALTIMESTAMP", nativeQuery = true)
    Instant getLocalTimestamp();

    List<Booking> findAllApprovedByBookerIdAndItemId(Long bookerId,Long itemId);
}
