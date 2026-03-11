package com.example.webcinemabooking.repository;

import com.example.webcinemabooking.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    // Chỉ cần để trống thế này, Spring Boot đã tự biết làm các lệnh Thêm, Sửa, Xóa, Tìm kiếm phim rồi!
}