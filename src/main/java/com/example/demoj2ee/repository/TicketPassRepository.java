package com.example.demoj2ee.repository;

import com.example.demoj2ee.model.TicketPass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketPassRepository extends JpaRepository<TicketPass, Long> {
    List<TicketPass> findByStatusOrderByCreatedAtDesc(String status);

    List<TicketPass> findByBookingId(Long bookingId);

    List<TicketPass> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    @Query("SELECT tp FROM TicketPass tp WHERE tp.status = 'AVAILABLE' " +
           "AND LOWER(tp.booking.showtime.movie.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY tp.createdAt DESC")
    List<TicketPass> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT tp FROM TicketPass tp WHERE tp.status = 'AVAILABLE' " +
           "AND tp.booking.showtime.id = :showtimeId " +
           "ORDER BY tp.createdAt DESC")
    List<TicketPass> findByShowtimeId(@Param("showtimeId") Long showtimeId);

    @Query("SELECT tp FROM TicketPass tp ORDER BY tp.createdAt DESC")
    List<TicketPass> findAllOrderByCreatedAtDesc();
}
