package com.example.demoj2ee.repository;

import com.example.demoj2ee.model.DatingMatch;
import com.example.demoj2ee.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DatingMatchRepository extends JpaRepository<DatingMatch, Long> {
    List<DatingMatch> findByUser1OrUser2(User user1, User user2);
    List<DatingMatch> findByUser1IdOrUser2Id(Long user1Id, Long user2Id);
    Optional<DatingMatch> findByUser1IdAndUser2Id(Long user1Id, Long user2Id);
    Optional<DatingMatch> findByUser1IdOrUser2IdAndIdNotNull(Long user1Id, Long user2Id);
    boolean existsByUser1IdAndUser2Id(Long user1Id, Long user2Id);
}
