package com.example.webcinemabooking.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "match_requests")
public class MatchRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Xin ghép đôi với bài đăng nào?
    @ManyToOne
    @JoinColumn(name = "cinema_date_id")
    private CinemaDate cinemaDate;

    // Ai là người đi xin ghép đôi?
    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;

    // Lời nhắn gạ gẫm (Comment)
    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String hostReply; // Lời đáp trả của người đăng bài

    private boolean isLikedByHost = false; // Chủ thớt có thả tym bình luận này không?
    // ------------------------------------------------

    // Trạng thái: PENDING (Đang chờ duyệt), ACCEPTED (Đồng ý), REJECTED (Từ chối)
    private String status = "PENDING";

    private LocalDateTime createdAt = LocalDateTime.now();
}