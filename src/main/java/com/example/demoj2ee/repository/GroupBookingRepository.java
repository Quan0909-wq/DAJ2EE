package com.example.demoj2ee.repository;

import com.example.demoj2ee.model.GroupBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupBookingRepository extends JpaRepository<GroupBooking, Long> {

    Optional<GroupBooking> findByRoomCode(String roomCode);

    List<GroupBooking> findByExpiresAtBefore(LocalDateTime now);

    List<GroupBooking> findByShowtimeIdAndStatus(Long showtimeId, int status);

    List<GroupBooking> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);

    List<GroupBooking> findByStatusOrderByCreatedAtDesc(int status);
}
