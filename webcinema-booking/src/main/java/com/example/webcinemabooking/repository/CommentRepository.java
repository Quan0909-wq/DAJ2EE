package com.example.webcinemabooking.repository;

import com.example.webcinemabooking.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Lấy danh sách bình luận của 1 phim, cái nào mới nhất hiện lên đầu
    List<Comment> findByMovieIdOrderByCreatedAtDesc(Long movieId);
}