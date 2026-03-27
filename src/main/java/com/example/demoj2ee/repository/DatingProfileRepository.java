package com.example.demoj2ee.repository;

import com.example.demoj2ee.model.DatingProfile;
import com.example.demoj2ee.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DatingProfileRepository extends JpaRepository<DatingProfile, Long> {
    Optional<DatingProfile> findByUser(User user);
    Optional<DatingProfile> findByUserId(Long userId);
    List<DatingProfile> findByIsActiveTrue();
    boolean existsByUserId(Long userId);
}
