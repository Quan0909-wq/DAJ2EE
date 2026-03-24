package com.example.demoj2ee.repository;

import com.example.demoj2ee.model.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
    // Tìm danh sách suất chiếu của một bộ phim cụ thể
    List<Showtime> findByMovieId(Long movieId);
}