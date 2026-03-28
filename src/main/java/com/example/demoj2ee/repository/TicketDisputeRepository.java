package com.example.demoj2ee.repository;

import com.example.demoj2ee.model.TicketDispute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketDisputeRepository extends JpaRepository<TicketDispute, Long> {

    List<TicketDispute> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<TicketDispute> findByStatusOrderByCreatedAtDesc(String status);

    List<TicketDispute> findAllByOrderByCreatedAtDesc();

    Optional<TicketDispute> findTopByBookingIdOrderByCreatedAtDesc(Long bookingId);
}
