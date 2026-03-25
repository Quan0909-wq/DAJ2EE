package com.example.webcinemabooking.repository;

import com.example.webcinemabooking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // MỚI THÊM: Import thư viện Query

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Lấy danh sách vé đã bán của một suất chiếu để chặn ghế
    List<Booking> findByShowtimeId(Long showtimeId);

    // MỚI THÊM: Tính tổng tiền tất cả hóa đơn (Doanh thu)
    @Query("SELECT SUM(b.totalAmount) FROM Booking b")
    Double sumTotalAmount();

    // MỚI THÊM: Đếm tổng số đơn hàng (Số vé đã bán)
    @Query("SELECT COUNT(b) FROM Booking b")
    Long countTotalBookings();
    // MỚI THÊM: Đếm số lượng khách hàng thực tế đã mua vé (Không trùng lặp)
    @Query("SELECT COUNT(DISTINCT b.customerName) FROM Booking b")
    Long countDistinctCustomers();
    // THÊM CHỮ "IgnoreCase" VÀO GIỮA TÊN HÀM
    List<Booking> findByCustomerEmailIgnoreCaseOrderByBookingTimeDesc(String customerEmail);
}