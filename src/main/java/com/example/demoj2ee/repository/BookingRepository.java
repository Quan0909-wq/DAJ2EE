package com.example.demoj2ee.repository;

import com.example.demoj2ee.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByShowtimeId(Long showtimeId);

    List<Booking> findByCustomerEmailIgnoreCaseOrderByBookingTimeDesc(String customerEmail);

    List<Booking> findByUserId(Long userId);

    Optional<Booking> findByUserIdAndShowtimeId(Long userId, Long showtimeId);

    @Query("SELECT DISTINCT b FROM Booking b " +
           "JOIN FETCH b.showtime s " +
           "JOIN FETCH s.movie " +
           "JOIN FETCH s.room " +
           "WHERE b.user.id = :userId " +
           "ORDER BY b.bookingTime DESC")
    List<Booking> findByUserIdWithDetails(@Param("userId") Long userId);
}
