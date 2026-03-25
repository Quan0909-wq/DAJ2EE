package com.example.webcinemabooking.repository;

import com.example.webcinemabooking.model.TicketPass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketPassRepository extends JpaRepository<TicketPass, Long> {
    // Tìm các vé ĐANG BÁN, xếp mới nhất lên đầu
    List<TicketPass> findByStatusOrderByCreatedAtDesc(String status);
}