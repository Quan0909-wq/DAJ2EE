package com.example.demoj2ee.repository;

import com.example.demoj2ee.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByTitleContainingIgnoreCase(String keyword);

    // THAY ĐỔI DÒNG NÀY: Thêm chữ Containing vào để nó tìm TƯƠNG ĐỐI
    List<Movie> findByGenreContainingIgnoreCase(String genre);
}