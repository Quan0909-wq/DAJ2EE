package com.example.demoj2ee.repository;

import com.example.demoj2ee.model.TicketPass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketPassRepository extends JpaRepository<TicketPass, Long> {
    List<TicketPass> findByStatusOrderByCreatedAtDesc(String status);
}