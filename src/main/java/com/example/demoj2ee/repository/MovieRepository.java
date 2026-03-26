package com.example.demoj2ee.repository;

import com.example.demoj2ee.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    // Tìm kiếm tương đối theo tên phim
    List<Movie> findByTitleContainingIgnoreCase(String keyword);

    // Tìm kiếm tương đối theo thể loại
    List<Movie> findByGenreContainingIgnoreCase(String genre);
}