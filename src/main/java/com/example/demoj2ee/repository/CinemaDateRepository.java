package com.example.demoj2ee.repository;

import com.example.demoj2ee.model.CinemaDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CinemaDateRepository extends JpaRepository<CinemaDate, Long> {
    List<CinemaDate> findByStatusOrderByCreatedAtDesc(String status);
    List<CinemaDate> findByMovieIdAndStatus(Long movieId, String status);
}
