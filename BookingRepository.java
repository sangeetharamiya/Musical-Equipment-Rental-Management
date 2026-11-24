package com.rental.music.repository;

import com.rental.music.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByEquipmentId(Long equipmentId);

    /**
     * Checks if any existing booking for the given equipment overlaps with [startDate, endDate].
     * Two inclusive date ranges [A, B] and [C, D] overlap iff A <= D AND B >= C.
     * excludeBookingId allows updating an existing booking without conflicting with itself.
     */
    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b
        WHERE b.equipment.id = :equipmentId
          AND (:excludeBookingId IS NULL OR b.id <> :excludeBookingId)
          AND b.startDate <= :endDate
          AND b.endDate >= :startDate
    """)
    boolean existsOverlappingBooking(
            @Param("equipmentId") Long equipmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeBookingId") Long excludeBookingId
    );

    List<Booking> findByEquipmentIdOrderByStartDateAsc(Long equipmentId);
}