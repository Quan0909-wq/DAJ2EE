package com.example.demoj2ee.repository;

import com.example.demoj2ee.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    Optional<Promotion> findByCode(String code);

    List<Promotion> findByActiveTrueOrderByCreatedAtDesc();

    List<Promotion> findByActiveTrueAndExpiresAtAfterOrderByCreatedAtDesc(LocalDateTime now);

    /** Tìm mã khuyến mãi đang hoạt động, chưa hết hạn theo mã. */
    @Query("SELECT p FROM Promotion p WHERE p.code = :code AND p.active = true " +
           "AND (p.expiresAt IS NULL OR p.expiresAt > :now)")
    Optional<Promotion> findValidByCode(String code, LocalDateTime now);
}
