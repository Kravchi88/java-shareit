package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long bookerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, Booking.BookingStatus status, Pageable pageable);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long ownerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(Long ownerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, Booking.BookingStatus status, Pageable pageable);

    boolean existsByBookerIdAndItemIdAndEndBefore(Long bookerId, Long itemId, LocalDateTime now);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.item.id = :itemId
             AND b.status = 'APPROVED'
             AND b.end < CURRENT_TIMESTAMP
            ORDER BY b.end DESC
            LIMIT 1
           """)
    Optional<Booking> findLastBooking(@Param("itemId") Long itemId);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.item.id = :itemId
             AND b.status = 'APPROVED'
             AND b.start > CURRENT_TIMESTAMP
            ORDER BY b.start ASC
            LIMIT 1
           """)
    Optional<Booking> findNextBooking(@Param("itemId") Long itemId);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.item.id IN :itemIds
             AND b.status = :status
           """)
    List<Booking> findAllByItemIdInAndStatus(@Param("itemIds") List<Long> itemIds, @Param("status") Booking.BookingStatus status);
}