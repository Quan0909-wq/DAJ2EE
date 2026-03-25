package com.example.webcinemabooking.repository;

import com.example.webcinemabooking.model.DisputeMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisputeMessageRepository extends JpaRepository<DisputeMessage, Long> {

    // Câu lệnh ma thuật của Spring Boot:
    // Tự động tìm TẤT CẢ tin nhắn thuộc về cái vé có id là "bookingId"
    // Và tự động sắp xếp (OrderBy) theo thời gian gửi (SentAt) từ cũ đến mới (Asc)
    List<DisputeMessage> findByBookingIdOrderBySentAtAsc(Long bookingId);

}