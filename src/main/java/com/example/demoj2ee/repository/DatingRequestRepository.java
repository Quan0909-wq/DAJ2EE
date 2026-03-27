package com.example.demoj2ee.repository;

import com.example.demoj2ee.model.DatingRequest;
import com.example.demoj2ee.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DatingRequestRepository extends JpaRepository<DatingRequest, Long> {
    List<DatingRequest> findByToUserAndStatus(User toUser, String status);
    List<DatingRequest> findByFromUser(User fromUser);
    List<DatingRequest> findByToUserIdAndStatus(Long toUserId, String status);
    List<DatingRequest> findByFromUserIdAndToUserId(Long fromUserId, Long toUserId);
    Optional<DatingRequest> findByFromUserIdAndToUserIdAndStatus(Long fromUserId, Long toUserId, String status);
    boolean existsByFromUserIdAndToUserIdAndStatus(Long fromUserId, Long toUserId, String status);
}
