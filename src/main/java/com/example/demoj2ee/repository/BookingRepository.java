package com.example.demoj2ee.repository;

import com.example.demoj2ee.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Lấy danh sách vé đã bán của một suất chiếu để chặn ghế
    List<Booking> findByShowtimeId(Long showtimeId);

    // Lịch sử mua vé của user
    List<Booking> findByCustomerEmailIgnoreCaseOrderByBookingTimeDesc(String customerEmail);
}