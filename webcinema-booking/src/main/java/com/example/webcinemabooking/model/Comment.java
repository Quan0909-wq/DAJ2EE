package com.example.webcinemabooking.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne // Nhiều bình luận thuộc về 1 bộ phim
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne // Nhiều bình luận được viết bởi 1 người dùng
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // Nội dung bình luận

    private int rating; // Số sao từ 1 đến 5

    private LocalDateTime createdAt = LocalDateTime.now(); // Ngày giờ bình luận
}