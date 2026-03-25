package com.example.demoj2ee.repository;

import com.example.demoj2ee.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByShowtimeId(Long showtimeId);
    List<Booking> findByCustomerEmailIgnoreCaseOrderByBookingTimeDesc(String customerEmail);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.status = 'SUCCESS'")
    Double sumTotalAmount();

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'SUCCESS'")
    long countTotalBookings();

    @Query("SELECT COUNT(DISTINCT b.customerEmail) FROM Booking b WHERE b.status = 'SUCCESS'")
    long countDistinctCustomers();

    List<Booking> findByCustomerNameContainingIgnoreCase(String name);
    List<Booking> findByStatus(String status);
}