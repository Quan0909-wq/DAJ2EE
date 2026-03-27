package com.example.demoj2ee.repository;

import com.example.demoj2ee.model.DatingNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DatingNotificationRepository extends JpaRepository<DatingNotification, Long> {
    List<DatingNotification> findByToUserIdOrderByCreatedAtDesc(Long toUserId);
    List<DatingNotification> findByToUserIdAndIsShownFalseOrderByCreatedAtDesc(Long toUserId);
    long countByToUserIdAndIsShownFalse(Long toUserId);
    long countByToUserIdAndIsReadFalse(Long toUserId);
    void deleteByToUserId(Long toUserId);
}
