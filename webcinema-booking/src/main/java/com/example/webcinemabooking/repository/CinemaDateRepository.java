package com.example.webcinemabooking.repository;

import com.example.webcinemabooking.model.CinemaDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CinemaDateRepository extends JpaRepository<CinemaDate, Long> {

    // Tìm những người đang ế (PENDING) để hiển thị lên giao diện
    List<CinemaDate> findByStatusOrderByCreatedAtDesc(String status);

    // Tìm các yêu cầu ghép đôi của một bộ phim cụ thể
    List<CinemaDate> findByMovieIdAndStatus(Long movieId, String status);
}