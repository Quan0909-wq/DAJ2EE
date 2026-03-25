package com.example.webcinemabooking.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cinema_dates")
public class CinemaDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ĐÃ GỠ BỎ nullable = false (Cực kỳ dễ dãi, cho phép lưu thoải mái)
    @ManyToOne
    @JoinColumn(name = "host_user_id")
    private User hostUser;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "showtime_id")
    private Showtime showtime;

    @Column(columnDefinition = "TEXT")
    private String bioMessage;

    @Column(nullable = false)
    private String status = "PENDING";

    @ManyToOne
    @JoinColumn(name = "matched_user_id")
    private User matchedUser;

    private LocalDateTime createdAt = LocalDateTime.now();
    // THÊM 2 DÒNG NÀY VÀO ĐỂ LẤY DANH SÁCH BÌNH LUẬN:
    @OneToMany(mappedBy = "cinemaDate", cascade = CascadeType.ALL)
    private java.util.List<MatchRequest> matchRequests;

    @ManyToMany
    @JoinTable(
            name = "cinema_date_likes",
            joinColumns = @JoinColumn(name = "cinema_date_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private java.util.Set<User> likedByUsers = new java.util.HashSet<>();

    // 2. HÀM KIỂM TRA: Xem tài khoản này đã thả tym bài này chưa?
    public boolean isLikedBy(User user) {
        if (user == null) return false;
        for (User u : likedByUsers) {
            if (u.getId().equals(user.getId())) return true;
        }
        return false;
    }
}
