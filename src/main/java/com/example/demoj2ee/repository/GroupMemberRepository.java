package com.example.demoj2ee.repository;

import com.example.demoj2ee.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByGroupBookingId(Long groupBookingId);

    List<GroupMember> findByGroupBookingIdOrderById(Long groupBookingId);

    long countByGroupBookingId(Long groupBookingId);

    Optional<GroupMember> findByGroupBookingIdAndEmail(Long groupBookingId, String email);

    List<GroupMember> findByEmail(String email);
}
