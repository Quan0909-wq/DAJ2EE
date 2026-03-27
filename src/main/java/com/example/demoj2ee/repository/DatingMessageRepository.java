package com.example.demoj2ee.repository;

import com.example.demoj2ee.model.DatingMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DatingMessageRepository extends JpaRepository<DatingMessage, Long> {
    List<DatingMessage> findByMatchIdOrderByCreatedAtAsc(Long matchId);
    List<DatingMessage> findByMatchIdOrderByCreatedAtDesc(Long matchId);
    long countByMatchIdAndIsReadFalse(Long matchId);
    long countByMatchIdAndSenderIdNotAndIsReadFalse(Long matchId, Long senderId);
}
