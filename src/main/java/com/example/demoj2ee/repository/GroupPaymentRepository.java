package com.example.demoj2ee.repository;

import com.example.demoj2ee.model.GroupPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupPaymentRepository extends JpaRepository<GroupPayment, Long> {

    List<GroupPayment> findByGroupBookingId(Long groupBookingId);

    Optional<GroupPayment> findByMemberId(Long memberId);

    List<GroupPayment> findByGroupBookingIdAndStatus(Long groupBookingId, String status);
}
