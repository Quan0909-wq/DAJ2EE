package com.example.demoj2ee.repository;

import com.example.demoj2ee.model.DisputeMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisputeMessageRepository extends JpaRepository<DisputeMessage, Long> {
    List<DisputeMessage> findByBookingIdOrderBySentAtAsc(Long bookingId);
}
