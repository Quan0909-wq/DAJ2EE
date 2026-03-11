package com.example.webcinemabooking.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // Tên phim (VD: Mai, Đào Phở và Piano)

    @Column(columnDefinition = "TEXT")
    private String description; // Nội dung tóm tắt phim
    private String director; // Đạo diễn

    private String cast; // Dàn diễn viên chính

    private int duration; // Thời lượng phim (tính bằng phút)

    private LocalDate releaseDate; // Ngày khởi chiếu

    private String posterUrl; // Link ảnh dọc (Poster dán ngoài rạp)

    private String trailerUrl; // Link video Youtube

}